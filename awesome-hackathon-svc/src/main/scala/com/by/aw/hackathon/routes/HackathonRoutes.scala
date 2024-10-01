package com.by.aw.hackathon.routes

import com.by.aw.hackathon.model.HealthCheck
import org.apache.pekko.http.scaladsl.model.StatusCodes
import org.apache.pekko.http.scaladsl.server.Directives.*
import org.apache.pekko.http.scaladsl.server.Route

import java.time.Instant

trait HackathonRoutes extends HackathonJsonFormat:

  def httpRoutes: Route = concat(health, hackathonRoutes)

  private def health: Route =
    path("health") {
      get {
        val health = HealthCheck("Hackathon Api is Healthy!!!", Instant.now())
        complete(StatusCodes.OK, health)
      }
    }

  private def hackathonRoutes: Route =
    pathPrefix("hackathon") {
      get {
        complete(StatusCodes.NotImplemented, "Hackathon api is yet not implemented")
      }
    }
