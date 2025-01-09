package it.unibo.scafi

import requests.*

import scala.io.Source

class GeminiService(val model: String, val apiKey: String, override val localKnowledge: String)
    extends CodeGeneratorService:
  override def generateCode(prompt: String): String =
    val url = s"https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
    val headers = Map("Content-Type" -> "application/json")
    val data =
      s"""|{
          |  "contents": [{
          |    "parts": [{"text": "$localKnowledge $prompt"}]
          |  }]
          |
          |}""".stripMargin
    val response = requests.post(url, headers = headers, data = data)
    // convert the response from byte to string
    response.text()

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
      localKnowledge: String = defaultLocalKnowledge,
  ): GeminiService =
    new GeminiService(s"gemini-$version-flash", apiKey, localKnowledge)

  def pro(
      version: Version,
      apiKey: String = defaultApiKey,
      localKnowledge: String = defaultLocalKnowledge,
  ): GeminiService =
    new GeminiService(s"gemini-$version-pro", apiKey, localKnowledge)
end GeminiService
