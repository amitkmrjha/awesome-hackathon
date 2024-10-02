package com.by.aw.hackathon.client.pybynder

import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.adapter.*
import sttp.capabilities.pekko.PekkoStreams
import sttp.client4.WebSocketStreamBackend
import sttp.client4.logging.slf4j.Slf4jLoggingBackend
import sttp.client4.pekkohttp.PekkoHttpBackend

import scala.concurrent.Future

trait PyBynderRestClient:
  def backend: WebSocketStreamBackend[Future, PekkoStreams]

object PyBynderRestClient:
  def apply(using typedSystem: ActorSystem[?]): PyBynderRestClient =
    new PyBynderRestClient:
      override val backend: WebSocketStreamBackend[Future, PekkoStreams] = Slf4jLoggingBackend(
        PekkoHttpBackend.usingActorSystem(typedSystem.toClassic)
      )
