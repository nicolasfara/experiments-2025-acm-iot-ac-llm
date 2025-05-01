package it.unibo.scafi.program.llm

import cats.effect.IO

trait CodeGeneratorService:
  def generateMain(localKnowledge: String, prompt: String): IO[String]
  def generateRaw(localKnowledge: String, preamble: String, prompt: String): IO[String]
