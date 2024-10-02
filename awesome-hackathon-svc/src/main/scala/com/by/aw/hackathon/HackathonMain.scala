package com.by.aw.hackathon

import com.by.aw.hackathon.aws.DefaultBedrockModel
import com.by.aw.hackathon.client.pybynder.PyBynderRestClient
import com.by.aw.hackathon.provider.DefaultAssetProvider
import com.by.aw.hackathon.service.HackathonServiceImpl
import com.typesafe.config.ConfigFactory
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.http.scaladsl.Http
import org.apache.pekko.management.cluster.bootstrap.ClusterBootstrap
import org.apache.pekko.management.scaladsl.PekkoManagement
import org.slf4j.LoggerFactory
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

object HackathonMain:
  val logger = LoggerFactory.getLogger(HackathonMain.getClass)

  lazy val config =
    ConfigFactory
      .parseString("pekko.http.server.preview.enable-http2 = on")
      .withFallback(ConfigFactory.load())

  given system: ActorSystem[?] = ActorSystem(Behaviors.empty, "awesome-hackathon-svc", config)
  given ec: ExecutionContext   = system.executionContext

  @main def main(): Unit =
    sys.addShutdownHook(system.terminate())
    val bedrockClient = BedrockRuntimeClient
      .builder()
      .region(Region.EU_CENTRAL_1)
      .credentialsProvider(ProfileCredentialsProvider.create("development"))
      .build()

    try {
      init(bedrockClient)
    } catch {
      case NonFatal(e) =>
        logger.error("Terminating due to initialization error", e)
        system.terminate()
    }

  def init(bedrockClient: BedrockRuntimeClient)(using
      system: ActorSystem[?]
  ): Unit =
    PekkoManagement(system).start()
    ClusterBootstrap(system).start()

    startGrpc(bedrockClient)

  private def startGrpc(bedrockClient: BedrockRuntimeClient)(using
      system: ActorSystem[?]
  ): Future[Http.ServerBinding] =
    given ActorSystem[?]   = system
    given ExecutionContext = system.executionContext

    val grpcInterface                       = system.settings.config.getString("awesome-hackathon-svc.grpc.interface")
    val grpcPort                            = system.settings.config.getInt("awesome-hackathon-svc.grpc.port")
    val bedrockModel                        = new DefaultBedrockModel(bedrockClient)
    val pyBynderRestClient                  = PyBynderRestClient.apply
    val assetProvider                       = new DefaultAssetProvider(pyBynderRestClient)
    val serviceImpl                         = new HackathonServiceImpl(bedrockModel, assetProvider)
    val binding: Future[Http.ServerBinding] = HackathonServer.start(grpcInterface, grpcPort, serviceImpl, serviceImpl)
    system.log.info(s"Awesome Hackathon gRPC and Http server running at $grpcInterface:$grpcPort")
    binding
