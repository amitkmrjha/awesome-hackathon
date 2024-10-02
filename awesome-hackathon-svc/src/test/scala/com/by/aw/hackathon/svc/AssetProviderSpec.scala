package com.by.aw.hackathon.svc

import com.by.aw.hackathon.client.pybynder.PyBynderRestClient
import com.by.aw.hackathon.client.pybynder.model.PyBynderModel.CollectionRequest
import com.by.aw.hackathon.provider.DefaultAssetProvider
import com.typesafe.config.ConfigFactory
import org.apache.pekko.actor.testkit.typed.scaladsl.ActorTestKit
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.http.scaladsl.model.HttpHeader
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.*
import org.scalatest.wordspec.AsyncWordSpec

import java.util.UUID
import scala.concurrent.Future
import scala.concurrent.duration.*

class AssetProviderSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll with ScalaFutures:

  given patience: PatienceConfig = PatienceConfig(scaled(5.seconds), scaled(100.millis))

  lazy val config =
    ConfigFactory
      .parseString("""
          |pekko.http.server.preview.enable-http2 = on
          |pekko.coordinated-shutdown.exit-jvm = off
          """.stripMargin)
      .withFallback(ConfigFactory.load("local1.conf"))
      .resolve()

  val testKit: ActorTestKit = ActorTestKit(config)

  given typedSystem: ActorSystem[?] = testKit.system

  val pyBynderRestClient = PyBynderRestClient.apply
  val assetProvider      = new DefaultAssetProvider(pyBynderRestClient)

  override def beforeAll(): Unit =
    super.beforeAll()
  override def afterAll(): Unit  =
    testKit.shutdownTestKit()

  "Asset provider" should {
    "say hello to pybynder" in {
      val cRequest = CollectionRequest("hello")
      val response = assetProvider.createCollection(cRequest, List.empty[HttpHeader])
      response.futureValue.size shouldBe 0
    }
  }
