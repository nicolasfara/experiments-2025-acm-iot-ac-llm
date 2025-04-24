package it.unibo.scafi.program.RAG

import dev.langchain4j.data.document.Document
import dev.langchain4j.data.document.DocumentParser
import dev.langchain4j.data.embedding.Embedding
import dev.langchain4j.store.embedding.EmbeddingStore
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.memory.ChatMemory
import dev.langchain4j.model.chat.StreamingChatLanguageModel
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.rag.content.retriever.ContentRetriever
import it.unibo.scafi.program.llm.langchain.models.LangChainModel

trait RAGTrait:
  def createChatLanguageModel(model: LangChainModel): StreamingChatLanguageModel
  def createDocumentParser(documentPath: String): DocumentParser
  def loadDocumentRAG(documentPath: String, documentParser:DocumentParser): Document
  def splitDocument(document: Document, chunkLength: Int, overlapLength: Int): List[TextSegment]
  def createEmbeddingModel(): EmbeddingModel
  def embeddSegments(listSegments: List[TextSegment], embeddingModel: EmbeddingModel): List[Embedding]
  def storeEmbedding(embeddings: List[Embedding], segments: List[TextSegment]): EmbeddingStore[TextSegment]
  def createContentRetriever(embeddingStore: EmbeddingStore[TextSegment], embeddingModel: EmbeddingModel, maxResult:Int, minScore:Double): ContentRetriever
  def createChatMemory(maxMessage: Int): ChatMemory
  def createAIService(chatLanguageModel: StreamingChatLanguageModel, contentRetriever: ContentRetriever, chatMemory: ChatMemory): Assistant