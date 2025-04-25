package it.unibo.scafi.program.utils

import java.net.{ URISyntaxException, URL }
import java.nio.file.{ Path, Paths }

object StringUtils:
  def toPath(relativePath: String): Path =
    val fileUrl: URL = getClass.getClassLoader.getResource(relativePath)
    if fileUrl == null then throw RuntimeException(s"Resource not found: $relativePath")
    try Paths.get(fileUrl.toURI)
    catch case e: URISyntaxException => throw RuntimeException(e)

  def refineOutput(originalOutput: String): String =
    val cleaned = originalOutput
      .replaceAll("```scala\n", "")
      .replaceAll("```", "")
      .replaceAll("`", "")
    cleaned
