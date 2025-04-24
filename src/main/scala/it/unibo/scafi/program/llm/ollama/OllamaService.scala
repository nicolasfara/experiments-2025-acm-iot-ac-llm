package it.unibo.scafi.program.llm.ollama

import io.github.ollama4j.OllamaAPI
import io.github.ollama4j.utils.OptionsBuilder
import it.unibo.scafi.program.llm.CodeGeneratorService
import it.unibo.scafi.program.llm.langchain.models.modelsEnum.OllamaModels
import it.unibo.scafi.program.utils.StringUtils

import scala.concurrent.{ExecutionContext, Future}

class OllamaService(val model: String, val host: String) extends CodeGeneratorService:
  private val mainPreamble =
    "Try to write (ONLY!!! PLEASE DO NOT ADD ANY OTHER LINES!!!) the body of the main (WITHOUT WRITE DEF MAIN()!!! AND WITHOUT CURLY BRACES IN MULTI-LINE PROGRAMS) for the following problem:"
  private lazy val ollama = OllamaAPI(host)
  private def data(localKnowledge: String, preamble: String, prompt: String) = s"$localKnowledge.\n$preamble\n$prompt"

  override def generateRaw(
      localKnowledge: String,
      preamble: String,
      prompt: String,
  ): ExecutionContext ?=> Future[String] =
    Future:
      val res = StringUtils.refineOutput(ollama.generate(model, data(localKnowledge, preamble, prompt), false, OptionsBuilder().build()).getResponse)
      println(res)
      res
  override def generateMain(localKnowledge: String, prompt: String): ExecutionContext ?=> Future[String] =
    generateRaw(localKnowledge, mainPreamble, prompt)
end OllamaService


object OllamaService:

  def of(
      name: OllamaModels,
      address: String
  ): OllamaService =
    new OllamaService(name.toString, address)
