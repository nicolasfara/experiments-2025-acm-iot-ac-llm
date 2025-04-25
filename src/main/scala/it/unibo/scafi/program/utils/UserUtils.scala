package it.unibo.scafi.program.utils

import scala.io.StdIn.readLine
import scala.sys.exit

object UserUtils:
  def getUserChoice(prompt: String, validOptions: Set[String]): String =
    val choice = readLine(prompt).trim.toUpperCase
    if validOptions.contains(choice) then choice
    else
      println(s"Invalid choice! You must enter one of: ${validOptions.mkString(", ")}")
      exit(1)
