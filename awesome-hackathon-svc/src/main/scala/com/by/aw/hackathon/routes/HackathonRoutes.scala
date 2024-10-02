package com.by.aw.hackathon.routes

import com.by.aw.hackathon.model.{HealthCheck, ModelRequest}
import com.by.aw.hackathon.service.HackathonHttpService
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.http.cors.scaladsl.CorsDirectives.cors
import org.apache.pekko.http.cors.scaladsl.settings.CorsSettings
import org.apache.pekko.http.scaladsl.model.StatusCodes
import org.apache.pekko.http.scaladsl.server.Directives.*
import org.apache.pekko.http.scaladsl.server.{ExceptionHandler, RejectionHandler, Route}
import org.apache.pekko.actor.typed.scaladsl.adapter.*
import org.apache.pekko.http.cors.scaladsl.model.HttpOriginMatcher
import org.apache.pekko.http.scaladsl.model.headers.HttpOrigin

import java.time.Instant
import scala.util.{Failure, Success}
import scala.concurrent.duration.*

trait HackathonRoutes extends HackathonJsonFormat:
  import org.apache.pekko.http.cors.scaladsl.CorsDirectives._
  val rejectionHandler = corsRejectionHandler.withFallback(RejectionHandler.default)
  val exceptionHandler = ExceptionHandler { case e: NoSuchElementException =>
    complete(StatusCodes.NotFound -> e.getMessage)
  }
  val handleErrors     = handleRejections(rejectionHandler) & handleExceptions(exceptionHandler)

  def httpRoutes(httpService: HackathonHttpService)(using system: ActorSystem[?]): Route =
    handleErrors {
      cors() {
        handleErrors {
          val corsSetting = CorsSettings
            .default(system.toClassic)
            .withAllowedOrigins(HttpOriginMatcher.*)
            .withAllowCredentials(false)
          cors(corsSetting)(
            withRequestTimeout(1.minute)(concat(health, hackathonRoutes(httpService)))
          )
        }
      }
    }

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
