package it.unibo.scafi

import scala.concurrent.{ExecutionContext, Future}

trait CodeGeneratorService:
  def generateMain(localKnowledge: String, prompt: String): ExecutionContext ?=> Future[String]
  def generateRaw(localKnowledge: String, preamble: String, prompt: String): ExecutionContext ?=> Future[String]
