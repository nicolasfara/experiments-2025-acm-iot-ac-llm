package it.unibo.scafi.program.llm.langchain.models

import dev.langchain4j.model.chat.StreamingChatLanguageModel
import dev.langchain4j.model.localai.LocalAiStreamingChatModel
import java.time

class LocalAiLangChainModel(modelName: String, val baseUrl: String) extends LangChainModel("", modelName):

  /*
    DOC LANGCHAIN4J: https://docs.langchain4j.dev/integrations/language-models/local-ai
    DOC LOCALAI: https://localai.io/
   */

  override def build(): StreamingChatLanguageModel =
    LocalAiStreamingChatModel.builder()
      .baseUrl(baseUrl)
      .modelName(modelName)
      .temperature(0.5)
      .timeout(time.Duration.ofMinutes(10))
      .build();

  override def toString: String =
    s"${modelName}"

