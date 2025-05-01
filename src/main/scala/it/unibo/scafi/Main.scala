package it.unibo.scafi

import java.nio.file.{ Files, Path }

import cats.effect.{ IO, IOApp }
import it.unibo.scafi.program.ChannelTest
import io.circe.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import it.unibo.scafi.test.toResultsPerModelAndKnowledge

object MyApp extends IOApp.Simple:
  require(System.getenv("GEMINI_API_KEY") != null, "GEMINI_API_KEY environment variable must be set")
  require(System.getenv("OPENROUTER_API_KEY") != null, "OPENROUTER_API_KEY environment variable must be set")

  override def run: IO[Unit] =
    val tests = program.listPrograms()
    assert(tests.size == 11)

    val a = tests.map(_.executeTest())
    val b = a.flatten.sequence
    b.flatMap(producesTestResults =>
      IO:
        val resultsPerModel = producesTestResults.toResultsPerModelAndKnowledge
        val destinationPath = Path.of("data", "generated")
        for ((modelName, knowledgeFile), results) <- resultsPerModel
        do
          val serializedResults = results.asJson.toString
          val file =
            destinationPath.resolve(
              s"${modelName.replaceAll("/", "_")}[${knowledgeFile.replaceAll("/", "_")}]_results.json",
            )
          Files.write(file, serializedResults.getBytes),
    )
  end run
end MyApp
