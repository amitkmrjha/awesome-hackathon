package com.by.aw.hackathon.aws

import com.by.aw.hackathon.model.{ModelRequest, ModelResponse}
import com.by.aw.hackathon.util.BedrockRequestBody
import org.apache.pekko.actor.typed.ActorSystem
import org.json.JSONObject
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest

import java.nio.charset.Charset

trait BedrockModel(bedrockClient: BedrockRuntimeClient):
  def invoke(request: ModelRequest): ModelResponse

class DefaultBedrockModel(bedrockClient: BedrockRuntimeClient)(using system: ActorSystem[?])
    extends BedrockModel(bedrockClient):
  override def invoke(request: ModelRequest): ModelResponse =
    val bedrockBody         = BedrockRequestBody
      .builder()
      .withModelId(request.modelId)
      .withPrompt(request.prompt)
      .withInferenceParameter("max_tokens_to_sample", 2048)
      .withInferenceParameter("temperature", 0.5)
      .withInferenceParameter("top_k", 250)
      .withInferenceParameter("top_p", 1)
      .build()
    val invokeModelRequest  = InvokeModelRequest
      .builder()
      .modelId(request.modelId)
      .body(SdkBytes.fromString(bedrockBody, Charset.defaultCharset()))
      .build()
    val invokeModelResponse = bedrockClient.invokeModel(invokeModelRequest)
    val result              = new JSONObject(invokeModelResponse.body().asUtf8String())
    ModelResponse(result.getString("completion"))
