package it.unibo.scafi

import requests.*

class GeminiService(val model: String, val apiKey: String, override val localKnowledge: String)
    extends CodeGeneratorService:

  override def generateCode(prompt: String): String =
    val url = s"https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent?key=$apiKey"
    val headers = Map("Content-Type" -> "application/json")
    val data =
      s"""{
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

  def flash(version: Version, apiKey: String, localKnowledge: String): GeminiService =
    new GeminiService(s"gemini-$version-flash", apiKey, localKnowledge)

  def pro(version: Version, apiKey: String, localKnowledge: String): GeminiService =
    new GeminiService(s"gemini-$version-pro", apiKey, localKnowledge)
