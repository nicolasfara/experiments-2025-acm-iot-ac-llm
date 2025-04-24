package it.unibo.scafi.program.llm.langchain.models

import dev.langchain4j.model.chat.StreamingChatLanguageModel

abstract class LangChainModel(val apiKey: String, val modelName: String):

  /** Abstracted builder() method */
  def build(): StreamingChatLanguageModel

  override def toString: String =
    s"$modelName"
