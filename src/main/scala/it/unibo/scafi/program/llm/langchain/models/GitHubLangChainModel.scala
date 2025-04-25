package it.unibo.scafi.program.llm.langchain.models

import dev.langchain4j.model.chat.StreamingChatLanguageModel
import dev.langchain4j.model.github.GitHubModelsStreamingChatModel

class GitHubLangChainModel(gitHubToken: String, modelName: String) extends LangChainModel(gitHubToken, modelName):

  /*
    DOC LANGCHAIN4J: https://docs.langchain4j.dev/integrations/language-models/github-models
    DOC GITHUB: https://docs.github.com/en/github-models
   */

  override def build(): StreamingChatLanguageModel =
    GitHubModelsStreamingChatModel.builder()
      .gitHubToken(gitHubToken)
      .modelName(modelName)
      .logRequestsAndResponses(true)
      .build()

  override def toString: String =
    s"$modelName"
