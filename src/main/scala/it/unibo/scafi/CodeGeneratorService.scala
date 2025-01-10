package it.unibo.scafi

import scala.concurrent.Future

trait CodeGeneratorService:
  def localKnowledge: String
  def generateCode(prompt: String): Future[String]
