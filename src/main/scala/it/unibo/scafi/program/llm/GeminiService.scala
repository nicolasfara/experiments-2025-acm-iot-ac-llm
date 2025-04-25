package it.unibo.scafi.program.llm

import scala.concurrent.{ ExecutionContext, Future, Promise }

import dev.langchain4j.model.chat.response.{ ChatResponse, StreamingChatResponseHandler }
import dev.langchain4j.model.googleai.GoogleAiGeminiStreamingChatModel
import it.unibo.scafi.program.utils.{ PromptUtils, StringUtils }

class GeminiService(model: Model) extends CodeGeneratorService:
  private val langchainModel = GoogleAiGeminiStreamingChatModel
    .builder()
    .maxRetries(10)
    .apiKey(System.getenv("GEMINI_API_KEY"))
    .temperature(0.5)
    .timeout(java.time.Duration.ofMinutes(10))
    .modelName(model.codeName)
    .build()

  override def generateRaw(
      localKnowledge: String,
      preamble: String,
      prompt: String,
  ): ExecutionContext ?=> Future[String] =
    val promise = Promise[String]()
    val fullPrompt = PromptUtils.generateLocalKnowledgePrompt(localKnowledge) + "\n" + preamble + "\n" + PromptUtils
      .generateTaskPrompt(prompt)

    langchainModel.chat(
      fullPrompt,
      new StreamingChatResponseHandler():
        override def onPartialResponse(partialResponse: String): Unit = ()
        //        println(s"LC PR ${langChainModel.toString}: \n$partialResponse")

        override def onCompleteResponse(completeResponse: ChatResponse): Unit =
          val cleaned = StringUtils.refineOutput(completeResponse.aiMessage().text())
          println(s"LC CA ${model.codeName}: \n$cleaned")
          promise.success(cleaned)

        override def onError(error: Throwable): Unit =
          println(s"LC ERR ${model.codeName}: \n$error")
          promise.failure(error),
    )
    promise.future
  end generateRaw

  override def generateMain(localKnowledge: String, prompt: String): ExecutionContext ?=> Future[String] =
    generateRaw(localKnowledge, PromptUtils.generatePreamblePrompt(), prompt)

  override def toString: String = model.codeName
end GeminiService
