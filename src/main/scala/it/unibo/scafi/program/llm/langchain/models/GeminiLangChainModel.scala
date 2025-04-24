package it.unibo.scafi.program.llm.langchain.models

import dev.langchain4j.model.chat.StreamingChatLanguageModel
import dev.langchain4j.model.googleai.GoogleAiGeminiStreamingChatModel

import java.time

class GeminiLangChainModel(apiKey: String, modelName: String) extends LangChainModel(apiKey, modelName):

  /*
    DOC LANGCHAIN4J: https://docs.langchain4j.dev/integrations/language-models/google-ai-gemini
    DOC GEMINI: https://ai.google.dev/gemini-api/docs?hl=it
   */

  override def build(): StreamingChatLanguageModel =
    GoogleAiGeminiStreamingChatModel.builder()
      .apiKey(apiKey)
      .temperature(0.5)
      .timeout(time.Duration.ofMinutes(10))
      .modelName(modelName)
      .build()

  override def toString: String =
    s"${modelName}"
