package it.unibo.scafi

import io.github.ollama4j.OllamaAPI
import io.github.ollama4j.utils.OptionsBuilder

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.io.Source

class OllamaService(val model: String, host: String = "http://localhost:11434/") extends CodeGeneratorService:
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
      ollama.generate(model, data(localKnowledge, preamble, prompt), false, OptionsBuilder().build()).getResponse
  override def generateMain(localKnowledge: String, prompt: String): ExecutionContext ?=> Future[String] =
    generateRaw(localKnowledge, mainPreamble, prompt)
end OllamaService

object OllamaService:

  def of(
      name: String,
  ): OllamaService =
    new OllamaService(name)
