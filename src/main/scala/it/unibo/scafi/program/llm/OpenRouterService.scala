package it.unibo.scafi.program.llm

import cats.effect.IO
import dev.langchain4j.model.chat.response.{ ChatResponse, StreamingChatResponseHandler }
import dev.langchain4j.model.openai.OpenAiStreamingChatModel
import it.unibo.scafi.program.utils.{ PromptUtils, StringUtils }
import org.slf4j.LoggerFactory

import java.time.Duration.ofSeconds

class OpenRouterService(openRouterModel: Model) extends CodeGeneratorService:
  private val logger = LoggerFactory.getLogger(this.getClass)
  private val url = "https://openrouter.ai/api/v1"
  private val model = OpenAiStreamingChatModel
    .builder()
    .baseUrl(url)
    .apiKey(System.getenv("OPENROUTER_API_KEY"))
    .modelName(openRouterModel.codeName)
    .timeout(ofSeconds(30))
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
      model.chat(
        fullPrompt,
        new StreamingChatResponseHandler:
          override def onPartialResponse(partialResponse: String): Unit = ()

          override def onCompleteResponse(completeResponse: ChatResponse): Unit =
            logger.debug(s"Received complete response from model ${openRouterModel.codeName}")
            val cleaned = StringUtils.refineOutput(completeResponse.aiMessage().text())
            cb(Right(cleaned))

          override def onError(error: Throwable): Unit =
            logger.error(s"Error while generating code with model ${openRouterModel.codeName}", error)
            cb(Left(error)),
      )
      Some(IO.unit) // Find a way to interrupt the model inside this IO...
  end generateRaw

  override def generateMain(localKnowledge: String, prompt: String): IO[String] =
    generateRaw(localKnowledge, PromptUtils.generatePreamblePrompt(), prompt)

  override def toString: String = openRouterModel.codeName
end OpenRouterService
