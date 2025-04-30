package it.unibo.scafi.program.llm

import cats.effect.IO

import dev.langchain4j.model.chat.response.{ ChatResponse, StreamingChatResponseHandler }
import dev.langchain4j.model.googleai.GoogleAiGeminiStreamingChatModel
import it.unibo.scafi.program.utils.{ PromptUtils, StringUtils }

class GeminiService(model: Model) extends CodeGeneratorService:
  private val langchainModel = GoogleAiGeminiStreamingChatModel
    .builder()
    .maxRetries(10)
    .apiKey(System.getenv("GEMINI_API_KEY"))
    .timeout(java.time.Duration.ofMinutes(10))
    .modelName(model.codeName)
    .build()

  override def generateRaw(
      localKnowledge: String,
      preamble: String,
      prompt: String,
  ): IO[String] = IO.async: cb =>
    val fullPrompt = s"""$localKnowledge
      |
      |$preamble
      |
      |$prompt
      |""".stripMargin
    IO:
      langchainModel.chat(
        fullPrompt,
        new StreamingChatResponseHandler():
          override def onPartialResponse(partialResponse: String): Unit = ()

          override def onCompleteResponse(completeResponse: ChatResponse): Unit =
            val cleaned = StringUtils.refineOutput(completeResponse.aiMessage().text())
            cb(Right(cleaned))

          override def onError(error: Throwable): Unit =
            cb(Left(error)),
      )
      None // Try a way to cancel the model execution
  end generateRaw

  override def generateMain(localKnowledge: String, prompt: String): IO[String] =
    generateRaw(localKnowledge, PromptUtils.generatePreamblePrompt(), prompt)

  override def toString: String = model.codeName
end GeminiService
