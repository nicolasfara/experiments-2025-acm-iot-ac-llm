package it.unibo.scafi

import cats.effect.{ IO, IOApp }

//import java.nio.file.{ Files, Path }
//import scala.concurrent.duration.DurationInt
//import scala.concurrent.{ Await, ExecutionContext, Future }
//import io.circe.*
//import io.circe.generic.auto.*
//import io.circe.syntax.*
//import it.unibo.scafi.program.{ ChannelTest, SCRTest }
//import it.unibo.scafi.test.{ toResultsPerModelAndKnowledge, AbstractScafiProgramTest }
//import org.slf4j.LoggerFactory
//
//import scala.concurrent.ExecutionContext.Implicits.global

object MyApp extends IOApp.Simple:
//  require(System.getenv("GEMINI_API_KEY") != null, "GEMINI_API_KEY environment variable must be set")
//  require(System.getenv("OPENROUTER_API_KEY") != null, "OPENROUTER_API_KEY environment variable must be set")
//  require(System.getenv("GITHUB_TOKEN") != null, "GITHUB_TOKEN environment variable must be set")

//  val logger = LoggerFactory.getLogger("Main")
//

  override def run: IO[Unit] =
    val tests = program.listPrograms()
    assert(tests.size == 11)
    
    val a = tests.map(_.executeTest())
    val b = a.flatten.parSequence
    b *> IO.println("All tests completed")

  //
//  val allResultsFuture = Future.sequence {
//    tests.map(e => Future.sequence(e.executeTest()))
//  }.map(_.flatten)
//
//  val producesTestResults = Await.result(allResultsFuture, 2.hour)
//  logger.info(s"Test completed with ${producesTestResults.size} results")
//  val resultsPerModel = producesTestResults.toResultsPerModelAndKnowledge
//
//  val destinationPath = Path.of("data", "generated")
//  for ((modelName, knowledgeFile), results) <- resultsPerModel
//  do
//    val serializedResults = results.asJson.toString
//    val file =
//      destinationPath.resolve(s"${modelName.replaceAll("/", "_")}[${knowledgeFile.replaceAll("/", "_")}]_results.json")
//    Files.write(file, serializedResults.getBytes)
end MyApp

