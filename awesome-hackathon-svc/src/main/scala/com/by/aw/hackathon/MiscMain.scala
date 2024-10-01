package com.by.aw.hackathon

import com.by.aw.hackathon.util.BedrockRequestBody
import com.typesafe.config.ConfigFactory
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.json.JSONObject
import org.slf4j.LoggerFactory
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient
import software.amazon.awssdk.services.bedrockruntime.model.{InvokeModelRequest, InvokeModelResponse}

import java.nio.charset.Charset
import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

object MiscMain:
  val logger = LoggerFactory.getLogger(HackathonMain.getClass)

  lazy val config =
    ConfigFactory
      .parseString("pekko.http.server.preview.enable-http2 = on")
      .withFallback(ConfigFactory.load())

  private val MODEL_ID = "anthropic.claude-v2"

  private val PROMPT =
    """
      |Generative AI refers to artificial intelligence systems that are capable of generating
      |novel content such as text, images, audio, video, and more, as opposed to just analyzing
      |existing data. The key aspect of generative AI is that it creates brand new outputs that
      |are unique and original, not just reproductions or remixes of existing content. Generative
      |AI leverages machine learning techniques like neural networks that are trained on large
      |datasets to build an understanding of patterns and structures within the data. It then
      |uses that knowledge to generate new artifacts that conform to those patterns but are not
      |identical copies, allowing for creativity and imagination. Prominent examples of generative
      |AI include systems like DALL-E that creates images from text descriptions, GPT-3 that
      |generates human-like text, and WaveNet that produces realistic synthetic voices. Generative
      |models hold great promise for assisting and augmenting human creativity across many domains
      |but also raise concerns about potential misuse if not thoughtfully implemented. Overall,
      |generative AI aims to mimic human creative abilities at scale to autonomously produce
      |high-quality, diverse, and novel content.
      |
      |Please create a single short paragraph so that a five-year-old child can understand.
      """.stripMargin

  given system: ActorSystem[?] = ActorSystem(Behaviors.empty, "nft-asset-svc", config)

  given ec: ExecutionContext = system.executionContext

  @main def main(): Unit =
    sys.addShutdownHook(system.terminate())

    try {
      init(system)
    } catch {
      case NonFatal(e) =>
        logger.error("Terminating due to initialization error", e)
        system.terminate()
    }

    def init(
        system: ActorSystem[?]
    ): Unit =
      val bedrockClient = BedrockRuntimeClient
        .builder()
        .region(Region.US_EAST_1)
        .credentialsProvider(ProfileCredentialsProvider.create("development"))
        .build()

      try {
        val bedrockBody = BedrockRequestBody
          .builder()
          .withModelId(MODEL_ID)
          .withPrompt(PROMPT)
          .withInferenceParameter("max_tokens_to_sample", 2048)
          .withInferenceParameter("temperature", 0.5)
          .withInferenceParameter("top_k", 250)
          .withInferenceParameter("top_p", 1)
          .build()

        val invokeModelRequest = InvokeModelRequest
          .builder()
          .modelId(MODEL_ID)
          .body(SdkBytes.fromString(bedrockBody, Charset.defaultCharset()))
          .build()

        val invokeModelResponse: InvokeModelResponse = bedrockClient.invokeModel(invokeModelRequest)
        val responseAsJson                           = new JSONObject(invokeModelResponse.body().asUtf8String())

        println("🤖 Response: ")
        println(responseAsJson.getString("completion"))

      } finally {
        bedrockClient.close()
      }
