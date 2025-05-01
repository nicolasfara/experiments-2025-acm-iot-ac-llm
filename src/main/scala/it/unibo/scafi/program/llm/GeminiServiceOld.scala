//package it.unibo.scafi.program.llm
//
//import cats.effect.IO
//
//import scala.concurrent.duration.*
//import scala.concurrent.{ ExecutionContext, Future }
//import io.circe.generic.auto.*
//import io.circe.parser.*
//import io.circe.syntax.*
//import it.unibo.scafi.*
//import requests.*
//import retry.Success
//import retry.Success.*
//
//private final case class Part(text: String)
//private final case class Content(parts: List[Part])
//private final case class Data(contents: List[Content])
//
//private final case class DataResponse(content: Content)
//private final case class Response(candidates: List[DataResponse])
//
//class GeminiServiceOld(val model: String, val apiKey: String) extends CodeGeneratorService:
//  private val mainPreamble =
//    "Try to write (ONLY!!!) the body of the main (WITHOUT WRITE DEF MAIN()!!! AND WITHOUT CURLY BRACES IN MULTI-LINE PROGRAMS) for the following problem:"
//  private given Success[requests.Response] = Success(_.statusCode == 200)
//  private val url = s"https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
//  private val headers = Map("Content-Type" -> "application/json")
//  private def data(localKnowledge: String, preamble: String, prompt: String) = Data(
//    List(
//      Content(
//        List(
//          Part(
//            s"$localKnowledge.\n$preamble\n$prompt",
//          ),
//        ),
//      ),
//    ),
//  ).asJson.noSpaces
//
//  override def generateRaw(
//      localKnowledge: String,
//      preamble: String,
//      prompt: String,
//  ): IO[String] = ???
////    for
////      response <- retry
////        .Pause(100, delay = 10.seconds)
////        .apply(Future {
////          requests.post(url, headers = headers, data = data(localKnowledge, preamble, prompt))
////        })
////      decodedPayload = decode[Response](response.text()) match
////        case Right(decoded) => decoded
////        case Left(error) => throw new RuntimeException(s"Failed to decode response $error")
////      cleaned = decodedPayload.candidates.head.content.parts.head.text
////        .replaceAll("```scala\n", "")
////        .replaceAll("```", "")
////        .replaceAll("`", "")
////    yield cleaned
//
//  override def generateMain(localKnowledge: String, prompt: String): IO[String] = ???
////    generateRaw(localKnowledge, mainPreamble, prompt)
//
//  override def toString: String = model
//end GeminiServiceOld
//
//object GeminiServiceOld:
//  enum Version:
//    case V1_5
//    case V2_0
//
//    override def toString: String =
//      this match
//        case V1_5 => "1.5"
//        case V2_0 => "2.0"
//
//  private lazy val defaultApiKey: String = System.getenv("GEMINI_API_KEY") match
//    case null => throw new RuntimeException("GEMINI_API_KEY is not set")
//    case apiKey => apiKey
//
//  def flashExp(
//      version: Version,
//      apiKey: String = defaultApiKey,
//  ): GeminiServiceOld =
//    new GeminiServiceOld(s"gemini-$version-flash-exp", apiKey)
//
//  def flash(
//      version: Version,
//      apiKey: String = defaultApiKey,
//  ): GeminiServiceOld =
//    new GeminiServiceOld(s"gemini-$version-flash", apiKey)
//
//  def thinking(
//      version: Version,
//      apiKey: String = defaultApiKey,
//  ): GeminiServiceOld =
//    new GeminiServiceOld(s"gemini-$version-flash-thinking-exp-01-21", apiKey)
//
//  def proExp(
//      version: Version,
//      apiKey: String = defaultApiKey,
//  ): GeminiServiceOld =
//    new GeminiServiceOld(s"gemini-$version-pro-exp-02-05", apiKey)
//
//end GeminiServiceOld
