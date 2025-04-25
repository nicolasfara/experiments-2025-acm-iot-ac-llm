package it.unibo.scafi.program.llm

import scala.concurrent.{ ExecutionContext, Future, Promise }

import dev.langchain4j.model.chat.response.{ ChatResponse, StreamingChatResponseHandler }
import dev.langchain4j.model.openai.OpenAiStreamingChatModel
import it.unibo.scafi.program.utils.{ PromptUtils, StringUtils }

class OpenRouterService(openRouterModel: Model) extends CodeGeneratorService:
  private val url = "https://openrouter.ai/api/v1"
  private val model = OpenAiStreamingChatModel
    .builder()
    .baseUrl(url)
    .apiKey(System.getenv("OPENROUTER_API_KEY"))
    .modelName(openRouterModel.codeName)
    .build()

  override def generateRaw(
      localKnowledge: String,
      preamble: String,
      prompt: String,
  ): ExecutionContext ?=> Future[String] =
    val promise = Promise[String]()
    val fullPrompt = s"""$localKnowledge
    |
    |$preamble
    |
    |$prompt
    |""".stripMargin

    model.chat(
      fullPrompt,
      new StreamingChatResponseHandler():
        override def onPartialResponse(partialResponse: String): Unit = ()

        override def onCompleteResponse(completeResponse: ChatResponse): Unit =
          val cleaned = StringUtils.refineOutput(completeResponse.aiMessage().text())
          println(s"OR CA ${this.toString}: \n$cleaned")
          promise.success(cleaned)

        override def onError(error: Throwable): Unit =
          println(s"OR ERR ${this.toString}: \n$error")
          promise.failure(error),
    )
    promise.future
  end generateRaw

  override def generateMain(localKnowledge: String, prompt: String): ExecutionContext ?=> Future[String] =
    generateRaw(localKnowledge, PromptUtils.generatePreamblePrompt(), prompt)

  override def toString: String = openRouterModel.codeName
end OpenRouterService
