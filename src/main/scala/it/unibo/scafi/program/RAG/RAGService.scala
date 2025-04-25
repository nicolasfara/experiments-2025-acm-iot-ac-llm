package it.unibo.scafi.program.RAG

import dev.langchain4j.data.document.{Document, DocumentParser}
import dev.langchain4j.data.embedding.Embedding
import dev.langchain4j.data.document.parser.TextDocumentParser
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.memory.ChatMemory
import dev.langchain4j.model.chat.StreamingChatLanguageModel
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.rag.content.retriever.{ContentRetriever, EmbeddingStoreContentRetriever}
import dev.langchain4j.store.embedding.EmbeddingStore
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocument
import dev.langchain4j.data.document.splitter.DocumentSplitters
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.chat.response.ChatResponse
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel
import dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel
import dev.langchain4j.model.embedding.onnx.bgesmallenv15.BgeSmallEnV15EmbeddingModel
import dev.langchain4j.model.embedding.onnx.bgesmallenv15q.BgeSmallEnV15QuantizedEmbeddingModel
import dev.langchain4j.service.AiServices
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore
import it.unibo.scafi.program.llm.CodeGeneratorService
import it.unibo.scafi.program.llm.langchain.models.LangChainModel
import it.unibo.scafi.program.utils.{PromptUtils, StringUtils}

import scala.jdk.CollectionConverters.*
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.jdk.CollectionConverters

class RAGService(langChainModel: LangChainModel, documentPath: String, config: RAGConfig = RAGConfig()) extends RAGTrait with CodeGeneratorService:

  private val rag = buildRag(langChainModel, documentPath)

  override def createChatLanguageModel(model: LangChainModel): StreamingChatLanguageModel =
    model.build()

  override def createDocumentParser(documentPath: String): DocumentParser =
    new TextDocumentParser()

  override def loadDocumentRAG(
                                documentPath: String,
                                documentParser: DocumentParser): Document =
    loadDocument(StringUtils.toPath(documentPath), documentParser)

  override def splitDocument(
                              document: Document,
                              chunkLength: Int,
                              overlapLength: Int): List[TextSegment] =
    DocumentSplitters.recursive(chunkLength, overlapLength)
      .split(document)
      .asScala
      .toList

  override def createEmbeddingModel(): EmbeddingModel = config.embeddingModel match
    case "BgeSmallEnV15Quantized" => new BgeSmallEnV15QuantizedEmbeddingModel()
    case "BgeSmallEnV15" => new BgeSmallEnV15EmbeddingModel()
    case "AllMiniLmL6V2Quantized" => new AllMiniLmL6V2QuantizedEmbeddingModel()
    case "AllMiniLmL6V2" => new AllMiniLmL6V2EmbeddingModel();

  override def embeddSegments(
                               segments: List[TextSegment],
                               model: EmbeddingModel): List[Embedding] =
    model.embedAll(segments.asJava).content().asScala.toList

  override def storeEmbedding(
                               embeddings: List[Embedding],
                               segments: List[TextSegment]): EmbeddingStore[TextSegment] =
    val store = new InMemoryEmbeddingStore[TextSegment]()
    store.addAll(embeddings.asJava, segments.asJava)
    store

  override def createContentRetriever(
                                       store: EmbeddingStore[TextSegment],
                                       model: EmbeddingModel,
                                       maxResult: Int,
                                       minScore: Double
                                     ): ContentRetriever =
    EmbeddingStoreContentRetriever.builder()
      .embeddingStore(store)
      .embeddingModel(model)
      .maxResults(maxResult)
      .minScore(minScore)
      .build()

  override def createChatMemory(maxMessages: Int): ChatMemory =
    MessageWindowChatMemory.withMaxMessages(maxMessages)


  override def createAIService(
                                chatModel: StreamingChatLanguageModel,
                                retriever: ContentRetriever,
                                memory: ChatMemory
                              ): Assistant =
    AiServices.builder(classOf[Assistant])
      .streamingChatLanguageModel(chatModel)
      .contentRetriever(retriever)
      .chatMemory(memory)
      .build()

  private def buildRag(
                        model: LangChainModel,
                        documentPath: String): Assistant =
    val modelLLM = createChatLanguageModel(model)
    val document = loadDocumentRAG(documentPath, createDocumentParser(documentPath))
    val textSegments = splitDocument(document, config.chunkSize, config.chunkOverlap)
    val embeddingModel = createEmbeddingModel()
    val embeddingList = embeddSegments(textSegments, embeddingModel)
    val embeddingStore = storeEmbedding(embeddingList, textSegments)
    val contentRetriever = createContentRetriever(embeddingStore, embeddingModel, config.maxResults, config.minScore)
    val chatMemory = createChatMemory(config.chatMemorySize)

    println(
      s"""RAG builded.
         |Params:
         |${config.toString}""".stripMargin
    )

    createAIService(modelLLM, contentRetriever, chatMemory)

  override def generateRaw(
                            localKnowledge: String,
                            preamble: String,
                            prompt: String): ExecutionContext ?=> Future[String] =
    val promise = Promise[String]()
    val fullPrompt = preamble + "\n" + PromptUtils.generateTaskPromptRAG(prompt)

    //println(s"Full prompt: ${fullPrompt}")

    val tokenStream = rag.chat(fullPrompt)
    tokenStream
      .onPartialResponse((partialResponse: String) =>
        println(s"RAG PR ${this.toString}: \n$partialResponse")
      )
      .onCompleteResponse((response: ChatResponse) =>
        val cleaned = StringUtils.refineOutput(response.aiMessage().text())
        println(s"RAG CA ${this.toString}: \n$cleaned")
        promise.success(cleaned))
      .onError((error: Throwable) =>
        println(s"RAG ERR ${this.toString}: \n$error")
        promise.failure(error)
      )
      .start()

    promise.future

  override def generateMain(localKnowledge: String, prompt: String): ExecutionContext ?=> Future[String] =
    generateRaw(localKnowledge, PromptUtils.generatePreamblePrompt(), prompt)

  override def toString: String =
    s"LLM: ${langChainModel.toString}, knowledgePath: $documentPath"

end RAGService

case class RAGConfig(
    chunkSize: Int = 512,
    chunkOverlap: Int = 128,
    maxResults: Int = 4,
    minScore: Double = 0.75,
    chatMemorySize: Int = 3,
    embeddingModel: String = "BgeSmallEnV15"
                    ):

  override def toString: String =
    s"""
       |Chunk size: $chunkSize
       |Chunk overlap: $chunkOverlap
       |Max Result: $maxResults
       |Min Score: $minScore
       |Chat memory size: $chatMemorySize
       |Embedding model: $embeddingModel
       |""".stripMargin



  








