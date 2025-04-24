package it.unibo.scafi.program.llm.langchain.models

import dev.langchain4j.model.chat.StreamingChatLanguageModel
import dev.langchain4j.model.ollama.OllamaStreamingChatModel

import java.time

class OllamaLangChainModel(modelName: String, val baseUrl: String) extends LangChainModel("", modelName):

  /*
    DOC LANGCHAIN4J: https://docs.langchain4j.dev/integrations/language-models/ollama
    DOC OLLAMA: https://github.com/ollama/ollama
   */

  override def build(): StreamingChatLanguageModel =
    OllamaStreamingChatModel.builder()
      .baseUrl(baseUrl)
      .temperature(0.5)
      .modelName(modelName)
      .timeout(time.Duration.ofMinutes(20))
      .build();

  override def toString: String =
    s"${modelName}"
