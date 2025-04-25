package it.unibo.scafi.program.RAG

import dev.langchain4j.service.{TokenStream, UserMessage}

trait Assistant:
    def chat(@UserMessage userMessage: String): TokenStream
