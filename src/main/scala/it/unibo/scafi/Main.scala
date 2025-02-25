package it.unibo.scafi

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.DurationInt
import io.circe.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import it.unibo.scafi.program.GradientWithDistanceFromSourceAndObstacles
import it.unibo.scafi.test.{toStatisticsPerModel, toStatisticsPerTest}

import java.nio.file.{Files, Path}
import java.util.concurrent.Executors

@main def main(): Unit =
  require(System.getenv("GEMINI_API_KEY") != null, "GEMINI_API_KEY environment variable must be set")
  val executor = Executors.newFixedThreadPool(1)
  given ExecutionContext = ExecutionContext.fromExecutor(executor)
  val tests = program.listPrograms()
//  val tests = List(GradientWithDistanceFromSourceAndObstacles())

  val allResultsFuture = Future
    .sequence:
      tests.map(e => Future.sequence(e.executeTest()))
    .map(_.flatten)

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
  val _ = executor.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS)
end main
