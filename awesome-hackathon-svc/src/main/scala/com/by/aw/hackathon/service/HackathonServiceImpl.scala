package com.by.aw.hackathon.service

import com.aw.hackathon.grpc.*
import com.by.aw.hackathon.aws.BedrockModel
import com.by.aw.hackathon.model.{ModelRequest, ModelResponse}
import com.by.aw.hackathon.provider.AssetProvider
import com.by.aw.hackathon.repository.SnowFlakeRepository
import com.google.protobuf.empty.Empty
import com.google.protobuf.timestamp.Timestamp
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.cluster.sharding.typed.scaladsl.ClusterSharding
import org.apache.pekko.grpc.scaladsl.Metadata
import org.apache.pekko.util.Timeout
import org.slf4j.LoggerFactory

import java.net.InetAddress
import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class HackathonServiceImpl[A: ActorSystem](
    bedrockModel: BedrockModel,
    assetProvider: AssetProvider,
    snowFlakeRepository: SnowFlakeRepository
) extends HackathonServicePowerApi
    with HackathonHttpService:

  val system   = summon[ActorSystem[?]]
  val log      = LoggerFactory.getLogger(getClass)
  val sharding = ClusterSharding(system)

  given ec: ExecutionContext = system.executionContext

  given timeout: Timeout = Timeout.create(system.settings.config.getDuration("awesome-hackathon-svc.grpc.ask-timeout"))

  private val hostname = InetAddress.getLocalHost.getHostAddress

  override def getHealth(in: Empty, metadata: Metadata): Future[GetHealthResponse] =
    Future.successful(
      GetHealthResponse("Awesome Hackathon gRPC is healthy!", hostname, Some(Timestamp(Instant.now())))
    )

  override def promptInvoke(request: ModelRequest): Future[ModelResponse] =
    val result = for {
      modelResponse  <- Future.fromTry(Try(bedrockModel.invoke(request)))
      assets         <- snowFlakeRepository.executeQuery(modelResponse.reply)
      collectionName <- Future.fromTry(Try(bedrockModel.invokeForCollectionName(request)))
    } yield ModelResponse(assets.mkString(","), Option(collectionName.reply))
    // modelResponse
    result.recover { case e: Exception =>
      log.error("Error while invoking model", e)
      ModelResponse(s"Model invocation failed: ${e.getMessage}")
    }

  def helloToPyBynder: Future[String] =
    Future.successful("Hello to Pybynder!")
