package com.by.aw.hackathon

import com.aw.hackathon.grpc.{HackathonService, HackathonServicePowerApi, HackathonServicePowerApiHandler}
import com.by.aw.hackathon.routes.HackathonRoutes
import com.by.aw.hackathon.service.HackathonHttpService
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.grpc.scaladsl.{ServerReflection, ServiceHandler}
import org.apache.pekko.http.scaladsl.Http
import org.apache.pekko.http.scaladsl.model.{HttpRequest, HttpResponse}
import org.apache.pekko.http.scaladsl.server.Directives.*
import org.apache.pekko.http.scaladsl.server.Route

import scala.concurrent.duration.*
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object HackathonServer extends HackathonRoutes:

  def start(interface: String, port: Int, grpcService: HackathonServicePowerApi, httpService: HackathonHttpService)(
      using system: ActorSystem[?]
  ): Future[Http.ServerBinding] =
    given ec: ExecutionContext = system.executionContext

    val service: HttpRequest => Future[HttpResponse] =
      ServiceHandler.concatOrNotFound(
        HackathonServicePowerApiHandler.partial(grpcService),
        // ServerReflection enabled to support grpcurl directly
        ServerReflection.partial(List(HackathonService))
      )

    val route: Route =
      val grpcRoute: Route = handle(service)
      val httpRoute: Route = httpRoutes(httpService)
      concat(httpRoute, grpcRoute)

    val bound =
      Http()
        .newServerAt(interface, port)
        .bind(route)
        .map(_.addToCoordinatedShutdown(3.seconds))

    bound.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info(
          "Awesome Hackathon is online with gRPC and Http server {}:{}",
          address.getHostName,
          address.getPort
        )
      case Failure(error)   =>
        system.log.error("Failed to bind gRPC endpoint, terminating system")
        system.terminate()
    }
    bound
