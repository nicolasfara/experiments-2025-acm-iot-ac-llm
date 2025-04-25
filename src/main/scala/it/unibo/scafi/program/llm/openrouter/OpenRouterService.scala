package it.unibo.scafi.program.llm.openrouter

import dev.langchain4j.model.chat.response.{ChatResponse, StreamingChatResponseHandler}
import dev.langchain4j.model.openai.OpenAiStreamingChatModel
import it.unibo.scafi.program.llm.CodeGeneratorService
import it.unibo.scafi.program.utils.{PromptUtils, StringUtils}

import scala.concurrent.{ExecutionContext, Future, Promise}

class OpenRouterService(openRouterModel:OpenRouterModels) extends CodeGeneratorService:
  /*
  DOC OPENROUTER: https://openrouter.ai/
  LIMITS:
    20 requests/minute
    200 requests/day
    https://github.com/cheahjs/free-llm-api-resources
   */

  private val url = s"https://openrouter.ai/api/v1"
  private val defaultApiKey: String = System.getenv("OPENROUTER_API_KEY") match
    case null => throw new RuntimeException("OPENROUTER_API_KEY is not set")
    case apiKey => apiKey

  private val model = OpenAiStreamingChatModel.builder().baseUrl(url).apiKey(defaultApiKey).modelName(openRouterModel.toString).build()

  override def generateRaw(localKnowledge: String, preamble: String, prompt: String): ExecutionContext ?=> Future[String] =
    val promise = Promise[String]()
    val fullPrompt = PromptUtils.generateLocalKnowledgePrompt(localKnowledge) + "\n" + preamble + "\n" + PromptUtils.generateTaskPrompt(prompt)

    model.chat(fullPrompt, new StreamingChatResponseHandler() {
      override def onPartialResponse(partialResponse: String): Unit =
        println(s"OR PR ${this.toString}: \n$partialResponse")
        //print("")

      override def onCompleteResponse(completeResponse: ChatResponse): Unit =
        val cleaned = StringUtils.refineOutput(completeResponse.aiMessage().text())
        println(s"OR CA ${this.toString}: \n$cleaned")
        promise.success(cleaned)

      override def onError(error: Throwable): Unit =
        println(s"OR ERR ${this.toString}: \n$error")
        promise.failure(error)
    })
    promise.future

  override def generateMain(localKnowledge: String, prompt: String): ExecutionContext ?=> Future[String] =
    generateRaw(localKnowledge, PromptUtils.generatePreamblePrompt(), prompt)

  override def toString: String =
    s"OPENROUTER) LLM: ${openRouterModel.toString}"

end OpenRouterService




