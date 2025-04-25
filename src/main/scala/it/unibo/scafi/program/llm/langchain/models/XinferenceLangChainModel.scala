package it.unibo.scafi.program.llm.langchain.models

import dev.langchain4j.community.model.xinference.XinferenceStreamingChatModel
import dev.langchain4j.model.chat.StreamingChatLanguageModel

import java.time

class XinferenceLangChainModel(modelName: String, val baseUrl: String) extends LangChainModel("", modelName):

  /*
    DOC LANGCHAIN4J: https://docs.langchain4j.dev/integrations/language-models/ollama
    DOC XInference: https://inference.readthedocs.io/en/latest/index.html
   */

  override def build(): StreamingChatLanguageModel =
    XinferenceStreamingChatModel.builder()
      .baseUrl(baseUrl)
      .temperature(0.5)
      .modelName(modelName)
      .timeout(time.Duration.ofMinutes(3))
      .build();

  override def toString: String =
    s"${modelName}"

