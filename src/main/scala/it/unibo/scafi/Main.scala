package it.unibo.scafi

import java.nio.file.{ Files, Path }
import scala.concurrent.duration.{ DurationInt, FiniteDuration }
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.concurrent.ExecutionContext.Implicits.global
import io.circe.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import it.unibo.scafi.program.{ ChannelTest, SCRTest }
import it.unibo.scafi.test.{ toStatisticsPerModel, toStatisticsPerTest, AbstractScafiProgramTest, SingleTestResult }

import java.util.concurrent.{ ScheduledThreadPoolExecutor, TimeUnit }

@main def main(): Unit =
  require(System.getenv("GEMINI_API_KEY") != null, "GEMINI_API_KEY environment variable must be set")
  require(System.getenv("OPENROUTER_API_KEY") != null, "OPENROUTER_API_KEY environment variable must be set")
//  require(System.getenv("GITHUB_TOKEN") != null, "GITHUB_TOKEN environment variable must be set")

  val scheduler = new ScheduledThreadPoolExecutor(1)

  val tests = program.listPrograms()
//  val tests = List(ChannelTest(), SCRTest())

  def delay(duration: FiniteDuration)(using ec: ExecutionContext): Future[Unit] =
    val promise = scala.concurrent.Promise[Unit]()
    scheduler.schedule(
      new Runnable:
        def run(): Unit = promise.success(())
      ,
      duration.toMillis,
      TimeUnit.MILLISECONDS,
    )
    promise.future

  def runScafiTestsSequentially(
      tests: List[AbstractScafiProgramTest],
  )(using ExecutionContext): Future[Seq[SingleTestResult]] =
    tests.foldLeft(Future.successful(Seq.empty[SingleTestResult])) { (accFuture, test) =>
      accFuture.flatMap { acc =>
        for
          _ <- delay(2.seconds) // Delay between tests for avoiding rate limits
          results <- Future.sequence(test.executeTest())
        yield acc ++ results
      }
    }
//  val limiter = RateLimiter(90, 10.seconds)
//  val allResultsFuture = Future.sequence {
//    tests.map(e => Future.sequence(e.executeTest()))
//  }.map(_.flatten)
  val allResultsFuture = runScafiTestsSequentially(tests)

  val producesTestResults = Await.result(allResultsFuture, 48.hour)
  val statisticsByModel = producesTestResults.toStatisticsPerModel
  val statisticByModelSerialized = statisticsByModel.asJson.toString
  val overallStatistics = producesTestResults.toStatisticsPerTest
  val overallStatisticsSerialized = overallStatistics.asJson.toString
  val serializedResult = producesTestResults.asJson.toString
  val destinationPath = Path.of("data", "generated")
  val datetime =
    java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))
  val destination =
    Files.write(destinationPath.resolve(s"test-results_$datetime.json"), serializedResult.getBytes)
  val _ =
    Files.write(destinationPath.resolve(s"test-statistics_$datetime.json"), statisticByModelSerialized.getBytes)
  val _ =
    Files.write(
      destinationPath.resolve(s"test-overall-statistics_$datetime.json"),
      overallStatisticsSerialized.getBytes,
    )
  println(s"Results written to $destination")
  println(s"Statistics by model: $statisticsByModel")
  println(s"Overall statistics: $overallStatistics")
//  val terminatingResult = executor.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS)
//  println(s"All tasks completed: $terminatingResult")
end main
