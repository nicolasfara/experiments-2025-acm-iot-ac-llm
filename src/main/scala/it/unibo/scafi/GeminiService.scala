package it.unibo.scafi

import requests.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import io.circe.parser.*
import retry.Success

import scala.io.Source
import scala.concurrent.duration.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import retry.Success.*

private final case class Part(text: String)
private final case class Content(parts: List[Part])
private final case class Data(contents: List[Content])

private final case class DataResponse(content: Content)
private final case class Response(candidates: List[DataResponse])

class GeminiService(val model: String, val apiKey: String) extends CodeGeneratorService:
  private val mainPreamble =
    "Try to write (ONLY!!!) the body of the main (WITHOUT WRITE DEF MAIN()!!! AND WITHOUT CURLY BRACES IN MULTI-LINE PROGRAMS) for the following problem:"
  private given Success[requests.Response] = Success(_.statusCode == 200)
  private val url = s"https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
  private val headers = Map("Content-Type" -> "application/json")
  private def data(localKnowledge: String, preamble: String, prompt: String) = Data(
    List(
      Content(
        List(
          Part(
            s"$localKnowledge.\n$preamble\n$prompt",
          ),
        ),
      ),
    ),
  ).asJson.noSpaces

  override def generateRaw(localKnowledge: String, preamble: String, prompt: String): Future[String] =
    for
      response <- retry
        .Backoff(5, delay = 1.seconds)
        .apply(Future {
          requests.post(url, headers = headers, data = data(localKnowledge, preamble, prompt))
        })
      decodedPayload = decode[Response](response.text()) match
        case Right(decoded) => decoded
        case Left(error) => throw new RuntimeException(s"Failed to decode response $error")
      cleaned = decodedPayload.candidates.head.content.parts.head.text
        .replaceAll("```scala\n", "")
        .replaceAll("\n```\n", "")
    yield cleaned
  override def generateMain(localKnowledge: String, prompt: String): Future[String] =
    generateRaw(localKnowledge, mainPreamble, prompt)
end GeminiService

object GeminiService:
  enum Version:
    case V1_5
    case V2_0

    override def toString: String =
      this match
        case V1_5 => "1.5"
        case V2_0 => "2.0"

  private lazy val defaultLocalKnowledge: String = Source.fromResource("knowledge.md").mkString
  private lazy val defaultApiKey: String = System.getenv("GEMINI_API_KEY") match
    case null => throw new RuntimeException("GEMINI_API_KEY is not set")
    case apiKey => apiKey

  def flash(
      version: Version,
      apiKey: String = defaultApiKey,
  ): GeminiService =
    new GeminiService(s"gemini-$version-flash-exp", apiKey)

  def pro(
      version: Version,
      apiKey: String = defaultApiKey,
      localKnowledge: String = defaultLocalKnowledge,
  ): GeminiService =
    new GeminiService(s"gemini-$version-pro", apiKey)
end GeminiService
