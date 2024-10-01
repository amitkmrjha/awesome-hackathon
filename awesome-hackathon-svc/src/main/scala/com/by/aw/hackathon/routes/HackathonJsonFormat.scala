package com.by.aw.hackathon.routes

import com.by.aw.hackathon.model.HealthCheck
import org.apache.pekko.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.*

import java.time.Instant

trait HackathonJsonFormat extends SprayJsonSupport with DefaultJsonProtocol:

  given TimestampFormat: RootJsonFormat[Instant] with
    override def read(json: JsValue): Instant = json match
      case JsString(value) =>
        Instant.parse(value)
      case _               => throw new DeserializationException("Expected timestamp")

    override def write(obj: Instant): JsValue = JsString(obj.toString)

  given healthCheckFormat: RootJsonFormat[HealthCheck] = jsonFormat2(HealthCheck.apply)
