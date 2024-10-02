package com.by.aw.hackathon.routes

import org.apache.pekko.http.scaladsl.model.HttpMethods._
import org.apache.pekko.http.scaladsl.model.headers._
import org.apache.pekko.http.scaladsl.model.{HttpResponse, StatusCodes}
import org.apache.pekko.http.scaladsl.server.Directives._
import org.apache.pekko.http.scaladsl.server.directives.RouteDirectives.complete
import org.apache.pekko.http.scaladsl.server.{Directive0, Route}
import scala.concurrent.duration._

trait CorsHandler:
  private lazy val accessControlAllowOrigin: String = sys.env.getOrElse("ACCESS_CONTROL_ALLOW_ORIGIN", "")

  private lazy val corsResponseHeaders = List(
    `Access-Control-Allow-Origin`.*,
    `Access-Control-Allow-Credentials`(true),
    `Access-Control-Allow-Headers`("Authorization",
      "Content-Type", "X-Requested-With"),
    `Access-Control-Max-Age`(1.day.toMillis)//Tell browser to cache OPTIONS requests
  )

  private def addAccessControlHeaders: Directive0 = {
    respondWithHeaders(corsResponseHeaders)
  }

  private def preflightRequestHandler: Route = options {
    complete(HttpResponse(StatusCodes.OK))
  }

  def corsHandler(r: Route): Route =
    if (accessControlAllowOrigin.nonEmpty) {
      addAccessControlHeaders {
        preflightRequestHandler ~ r
      }
    } else {
      r
    }
