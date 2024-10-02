package com.by.aw.hackathon.routes

import com.by.aw.hackathon.model.{HealthCheck, ModelRequest}
import com.by.aw.hackathon.service.HackathonHttpService
import org.apache.pekko.http.scaladsl.model.StatusCodes
import org.apache.pekko.http.scaladsl.server.Directives.*
import org.apache.pekko.http.scaladsl.server.Route

import java.time.Instant
import scala.util.{Failure, Success}

trait HackathonRoutes extends HackathonJsonFormat with CorsHandler:

  def httpRoutes(httpService: HackathonHttpService): Route =
    corsHandler(
      concat(health, hackathonRoutes(httpService))
    )

  private def health: Route =
    path("health") {
      get {
        val health = HealthCheck("Hackathon Api is Healthy!!!", Instant.now())
        complete(StatusCodes.OK, health)
      }
    }

  private def hackathonRoutes(httpService: HackathonHttpService): Route =
    pathPrefix("hackathon") {
      post {
        entity(as[ModelRequest]) { modelRequest =>
          onComplete(httpService.promptInvoke(modelRequest)) {
            case Success(modelResponse) => complete(StatusCodes.OK, modelResponse)
            case Failure(exception)     => complete(StatusCodes.InternalServerError, exception.getMessage)
          }
        }
      }
    }
