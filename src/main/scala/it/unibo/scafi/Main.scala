package it.unibo.scafi

import java.nio.file.{ Files, Path }
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ Await, ExecutionContext, Future }
import io.circe.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import it.unibo.scafi.program.{ ChannelTest, SCRTest }
import it.unibo.scafi.test.{ toResultsPerModel, AbstractScafiProgramTest }
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global

@main def main(): Unit =
  require(System.getenv("GEMINI_API_KEY") != null, "GEMINI_API_KEY environment variable must be set")
  require(System.getenv("OPENROUTER_API_KEY") != null, "OPENROUTER_API_KEY environment variable must be set")
//  require(System.getenv("GITHUB_TOKEN") != null, "GITHUB_TOKEN environment variable must be set")

  val logger = LoggerFactory.getLogger("Main")

  val tests = program.listPrograms()
  assert(tests.size == 11)

  val allResultsFuture = Future.sequence {
    tests.map(e => Future.sequence(e.executeTest()))
  }.map(_.flatten)

  val producesTestResults = Await.result(allResultsFuture, 2.hour)
  logger.info(s"Test completed with ${producesTestResults.size} results")
  val resultsPerModel = producesTestResults.toResultsPerModel

  val destinationPath = Path.of("data", "generated")
  for (modelName, results) <- resultsPerModel
  do
    val serializedResults = results.asJson.toString
    val file = destinationPath.resolve(s"${modelName.replaceAll("/", "_")}_results.json")
    Files.write(file, serializedResults.getBytes)

//  val statisticsByModel = producesTestResults.toStatisticsPerModel
//  val statisticByModelSerialized = statisticsByModel.asJson.toString
//  val overallStatistics = producesTestResults.toStatisticsPerTest
//  val overallStatisticsSerialized = overallStatistics.asJson.toString
//  val serializedResult = producesTestResults.asJson.toString
//  val destinationPath = Path.of("data", "generated")
//  val datetime =
//    java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))
//  val destination =
//    Files.write(destinationPath.resolve(s"test-results_$datetime.json"), serializedResult.getBytes)
//  val _ =
//    Files.write(destinationPath.resolve(s"test-statistics_$datetime.json"), statisticByModelSerialized.getBytes)
//  val _ =
//    Files.write(
//      destinationPath.resolve(s"test-overall-statistics_$datetime.json"),
//      overallStatisticsSerialized.getBytes,
//    )
//  println(s"Results written to $destination")
//  println(s"Statistics by model: $statisticsByModel")
//  println(s"Overall statistics: $overallStatistics")
//  val _ = scheduler.shutdownNow()
//  val terminatingResult = executor.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS)
//  println(s"All tasks completed: $terminatingResult")
end main
