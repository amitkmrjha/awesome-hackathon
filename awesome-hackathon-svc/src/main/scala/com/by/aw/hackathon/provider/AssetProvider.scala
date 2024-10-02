package com.by.aw.hackathon.provider

import com.by.aw.hackathon.client.pybynder.model.PyBynderModel.CollectionRequest
import com.by.aw.hackathon.client.pybynder.{PyBynderJsonFormat, PyBynderRestClient}
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.http.scaladsl.model.HttpHeader
import sttp.client4
import sttp.client4.*
import sttp.client4.sprayJson.*
import sttp.model.Header as SttpHttpHeader

import scala.concurrent.{ExecutionContext, Future}

trait AssetProvider:
  def createCollection(
      payload: CollectionRequest,
      headers: List[HttpHeader]
  ): Future[String]

class DefaultAssetProvider(pyBynderClient: PyBynderRestClient)(using system: ActorSystem[?])
    extends AssetProvider
    with PyBynderJsonFormat:

  given ec: ExecutionContext = system.executionContext
  val log                    = system.log
  val backend                = pyBynderClient.backend

  override def createCollection(payload: CollectionRequest, headers: List[HttpHeader]): Future[String] =
    val requestHeaders = headers.map { header =>
      SttpHttpHeader(header.name(), header.value())
    } ++ Seq(
      // SttpHttpHeader("Authorization", s"Bearer ${legacyAuthConfig.getString("idp.internal_api_token")}"),
      SttpHttpHeader("content-type", "application/x-www-form-urlencoded")
    )

    val host    = "dylan-ec2-devenv.bynder.io"
    val path    = uri"https://${host}/api/collection/hackathon"
    val request = basicRequest
      .body(payload)
      .post(path)
      .withHeaders(requestHeaders)
      .response(asJson[String])

    log.info(s"Request to pybynder: ${request.toString()}")

    val result = for
      response <- request.send(backend)
      body     <- response.body match
                    case Right(value) => Future.successful(value)
                    case Left(value)  =>
                      Future.failed(new Exception(response.toString()))
    yield body
    result.recoverWith { case e: Exception =>
      log.error(s"Error while calling pybynder: ${e.getMessage}")
      Future.failed(e)
    }
