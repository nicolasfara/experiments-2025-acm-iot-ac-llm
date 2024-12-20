package it.unibo.scafi

import requests.*

class GeminiService(val apiKey: String, override val localKnowledge: String) extends CodeGeneratorService:

  override def generateCode(prompt: String): String =
    val url = s"https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"
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
