package it.unibo.scafi

trait CodeGeneratorService:
  def localKnowledge: String
  def generateCode(prompt: String): String
