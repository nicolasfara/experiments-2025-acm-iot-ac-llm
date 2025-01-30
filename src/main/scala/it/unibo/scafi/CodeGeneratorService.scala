package it.unibo.scafi

import scala.concurrent.Future

trait CodeGeneratorService:
  def generateMain(localKnowledge: String, prompt: String): Future[String]
  def generateRaw(localKnowledge: String, preamble: String, prompt: String): Future[String]
