package com.by.aw.hackathon.aws

import com.by.aw.hackathon.model.{ModelRequest, ModelResponse}
import com.by.aw.hackathon.util.BedrockRequestBody
import com.by.aw.hackathon.util.PromptBuilder.{buildCollectionNamePrompt, buildPrompt}
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.event.slf4j.Logger
import org.json.JSONObject
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest

import java.nio.charset.Charset
import scala.util.matching.Regex

trait BedrockModel(bedrockClient: BedrockRuntimeClient):
  def invoke(request: ModelRequest): ModelResponse
  def invokeForCollectionName(request: ModelRequest): ModelResponse

class DefaultBedrockModel(bedrockClient: BedrockRuntimeClient)(using system: ActorSystem[?])
    extends BedrockModel(bedrockClient):

  val logger = Logger(this.getClass.getName)

  override def invoke(request: ModelRequest): ModelResponse =
    val bedrockBody              = BedrockRequestBody
      .builder()
      .withModelId(request.modelId)
      .withPrompt(buildPrompt(request.prompt))
      // .withPrompt(request.prompt)
      .withInferenceParameter("max_tokens_to_sample", 2048)
      .withInferenceParameter("temperature", 0.5)
      .withInferenceParameter("top_k", 250)
      .withInferenceParameter("top_p", 1)
      .build()
    val invokeModelRequest       = InvokeModelRequest
      .builder()
      .modelId(request.modelId)
      .body(SdkBytes.fromString(bedrockBody, Charset.defaultCharset()))
      .build()
    val invokeModelResponse      = bedrockClient.invokeModel(invokeModelRequest)
    val resultJson               = new JSONObject(invokeModelResponse.body().asUtf8String())
    val textString               = resultJson.getJSONArray("content").getJSONObject(0).getString("text")
    val sqlPattern: Regex        = """(?s)```sql\s*(.*?)\s*```""".r
    val sqlQuery: Option[String] = sqlPattern.findFirstMatchIn(textString).map(_.group(1).replaceAll("\\s+", " "))
    sqlQuery match {
      case Some(query) =>
        logger.info(s"Extracted SQL Query: $query")
        ModelResponse(query)
      case None        =>
        val msg = "No SQL query found in the response."
        logger.error(msg)
        ModelResponse(msg)
    }

  override def invokeForCollectionName(request: ModelRequest): ModelResponse =
    val bedrockBody                = BedrockRequestBody
      .builder()
      .withModelId(request.modelId)
      .withPrompt(buildCollectionNamePrompt(request.prompt))
      // .withPrompt(request.prompt)
      .withInferenceParameter("max_tokens_to_sample", 2048)
      .withInferenceParameter("temperature", 0.5)
      .withInferenceParameter("top_k", 250)
      .withInferenceParameter("top_p", 1)
      .build()
    val invokeModelRequest         = InvokeModelRequest
      .builder()
      .modelId(request.modelId)
      .body(SdkBytes.fromString(bedrockBody, Charset.defaultCharset()))
      .build()
    val invokeModelResponse        = bedrockClient.invokeModel(invokeModelRequest)
    val resultJson                 = new JSONObject(invokeModelResponse.body().asUtf8String())
    val textString                 = resultJson.getJSONArray("content").getJSONObject(0).getString("text")
    val namePattern: Regex         = """(?s)```\s*(.*?)\s*```""".r
    val nameString: Option[String] = namePattern.findFirstMatchIn(textString).map(_.group(1).replaceAll("\\s+", " "))
    nameString match {
      case Some(str) =>
        logger.info(s"Extracted Name: $str")
        ModelResponse(str)
      case None      =>
        val msg = "No Name found in the response."
        logger.error(msg)
        ModelResponse(msg)
    }
