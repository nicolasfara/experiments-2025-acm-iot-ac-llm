package it.unibo.scafi

import java.util.concurrent.Executors
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.concurrent.duration.DurationInt
import io.circe.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import it.unibo.scafi.program.CollectMaxIdTest
import it.unibo.scafi.test.{ toStatistcsPerModel, toStatisticsPerTest }

import java.nio.file.{ Files, Path }

@main def main(): Unit =
  val executor = Executors.newFixedThreadPool(4)
  given ExecutionContext = ExecutionContext.fromExecutor(executor)
  val tests = program.listPrograms()

  val allResultsFuture = Future
    .sequence:
      tests.map(e => Future.sequence(e.executeTest()))
    .map(_.flatten)

  val producesTestResults = Await.result(allResultsFuture, 10.minutes)
  val statisticsByModel = producesTestResults.toStatistcsPerModel()
  val statisticByModelSerialized = statisticsByModel.asJson.toString
  val overallStatistics = producesTestResults.toStatisticsPerTest()
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
  executor.shutdown()
end main
