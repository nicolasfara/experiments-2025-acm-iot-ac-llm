package it.unibo.scafi

import java.nio.file.{ Files, Path }

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.concurrent.ExecutionContext.Implicits.global

import io.circe.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import it.unibo.scafi.program.ChannelTest
import it.unibo.scafi.test.{ toStatisticsPerModel, toStatisticsPerTest }

@main def main(): Unit =
  require(System.getenv("GEMINI_API_KEY") != null, "GEMINI_API_KEY environment variable must be set")
  require(System.getenv("OPENROUTER_API_KEY") != null, "OPENROUTER_API_KEY environment variable must be set")
//  require(System.getenv("GITHUB_TOKEN") != null, "GITHUB_TOKEN environment variable must be set")

   val tests = program.listPrograms()
//  val tests = List(ChannelTest())

  val allResultsFuture = Future.sequence {
    tests.map(e => Future.sequence(e.executeTest()))
  }.map(_.flatten)

  val producesTestResults = Await.result(allResultsFuture, 2.hour)
  val statisticsByModel = producesTestResults.toStatisticsPerModel
  val statisticByModelSerialized = statisticsByModel.asJson.toString
  val overallStatistics = producesTestResults.toStatisticsPerTest
  val overallStatisticsSerialized = overallStatistics.asJson.toString
  val serializedResult = producesTestResults.asJson.toString
  val destinationPath = Path.of("data", "generated")
  val destination = Files.write(destinationPath.resolve("test-results.json"), serializedResult.getBytes)
  val _ =
    Files.write(destinationPath.resolve("test-statistics.json"), statisticByModelSerialized.getBytes)
  val _ =
    Files.write(destinationPath.resolve("test-overall-statistics.json"), overallStatisticsSerialized.getBytes)
  println(s"Results written to $destination")
  println(s"Statistics by model: $statisticsByModel")
  println(s"Overall statistics: $overallStatistics")
//  val terminatingResult = executor.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS)
//  println(s"All tasks completed: $terminatingResult")
end main
