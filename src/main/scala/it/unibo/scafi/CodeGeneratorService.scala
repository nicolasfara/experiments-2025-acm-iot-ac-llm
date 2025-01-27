package it.unibo.scafi

import scala.concurrent.Future

trait CodeGeneratorService:
  def generateCode(localKnowledge: String, prompt: String): Future[String]
