package it.unibo.scafi

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.DurationInt
import io.circe.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import it.unibo.scafi.program.GradientWithDistanceFromSourceAndObstacles
import it.unibo.scafi.program.utils.UserUtils
import it.unibo.scafi.test.{toStatisticsPerModel, toStatisticsPerTest}

import java.nio.file.{Files, Path}
import java.util.concurrent.Executors
import scala.io.StdIn.readLine

@main def main(): Unit =
  require(System.getenv("GEMINI_API_KEY") != null, "GEMINI_API_KEY environment variable must be set")
  require(System.getenv("OPENROUTER_API_KEY") != null, "OPENROUTER_API_KEY environment variable must be set")
  require(System.getenv("GITHUB_TOKEN") != null, "GITHUB_TOKEN environment variable must be set")

  val executor = Executors.newFixedThreadPool(1)
  given ExecutionContext = ExecutionContext.fromExecutor(executor)
  //val tests = program.listPrograms()
  val tests = List(GradientWithDistanceFromSourceAndObstacles())

  val method = UserUtils.getUserChoice("Choose execution method (LLM/RAG): ", Set("LLM", "RAG"))

  val subMethod = method match
    case "LLM" => UserUtils.getUserChoice("Choose subMode (API REST/LangChain/OpenRouter): ", Set("API REST", "LANGCHAIN", "OPENROUTER"))
    case "RAG" => UserUtils.getUserChoice("Choose subMode (Gemini/Ollama): ", Set("GEMINI", "OLLAMA"))

  val langChainType = subMethod match
    case "LANGCHAIN" => UserUtils.getUserChoice("Choose LangChain method (Ollama/Gemini/GitHub/Xinference): ", Set("OLLAMA", "GEMINI", "GITHUB", "XINFERENCE"))
    case _ => ""

  val ngrokAddress = (langChainType, subMethod) match
    case ("OLLAMA", _) | ("XINFERENCE", _) | (_, "OLLAMA") => readLine("Insert ngrok/localhost Ollama/Xinference address: ").trim
    case _ => ""

  println(s"Method selected: $method")
  println(s"SubMethod selected: $subMethod")
  println(s"LangChain method selected: $langChainType")
  println(s"Ngrok/localhost Ollama/Xinference address: $ngrokAddress")

  val allResultsFuture = Future.sequence {
    tests.map(e => Future.sequence(e.executeTest(method, subMethod, langChainType, ngrokAddress)))
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
  val _ = executor.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS)
end main
