package it.unibo.scafi.program.llm.langchain

import dev.langchain4j.model.chat.response.{ChatResponse, StreamingChatResponseHandler}
import it.unibo.scafi.program.llm.CodeGeneratorService
import it.unibo.scafi.program.utils.StringUtils
import it.unibo.scafi.program.utils.PromptUtils
import it.unibo.scafi.program.llm.langchain.models.LangChainModel

import scala.concurrent.{ExecutionContext, Future, Promise}

class LangChainService(langChainModel: LangChainModel) extends CodeGeneratorService:

  private val model = langChainModel.build()

  override def generateRaw(localKnowledge: String, preamble: String, prompt: String): ExecutionContext ?=> Future[String] =
    val promise = Promise[String]()
    val fullPrompt = PromptUtils.generateLocalKnowledgePrompt(localKnowledge) + "\n" + preamble + "\n" + PromptUtils.generateTaskPrompt(prompt)

    model.chat(fullPrompt, new StreamingChatResponseHandler() {
      override def onPartialResponse(partialResponse: String): Unit =
        println(s"LC PR ${langChainModel.toString}: \n$partialResponse")

      override def onCompleteResponse(completeResponse: ChatResponse): Unit =
        val cleaned = StringUtils.refineOutput(completeResponse.aiMessage().text())
        println(s"LC CA ${langChainModel.toString}: \n$cleaned")
        promise.success(cleaned)

      override def onError(error: Throwable): Unit =
        println(s"LC ERR ${langChainModel.toString}: \n$error")
        promise.failure(error)
    })
    promise.future

  override def generateMain(localKnowledge: String, prompt: String): ExecutionContext ?=> Future[String] =
    generateRaw(localKnowledge, PromptUtils.generatePreamblePrompt(), prompt)

  override def toString: String =
    s"LANGCHAIN) LLM: ${langChainModel.toString}"

