package it.unibo.scafi.test

import io.circe.generic.auto.*
import io.circe.parser.*
import it.unibo.scafi.program.RAG.RAGService
import it.unibo.scafi.program.llm.openrouter.{OpenRouterModels, OpenRouterService}
import it.unibo.scafi.program.llm.{CodeGeneratorService, GeminiService}
import it.unibo.scafi.program.llm.langchain.LangChainService
import it.unibo.scafi.program.llm.langchain.models.{GeminiLangChainModel, GitHubLangChainModel, LangChainModel, LocalAiLangChainModel, OllamaLangChainModel, XinferenceLangChainModel}
import it.unibo.scafi.test.ScafiTestUtils.{buildProgram, executeFromString}
import it.unibo.scafi.test.FunctionalTestIncarnation.Network
import it.unibo.scafi.test.ScafiTestResult.{CompilationFailed, GenericFailure}
import it.unibo.scafi.Prompts
import it.unibo.scafi.program.llm.langchain.models.modelsEnum.{GitHubModels, OllamaModels, XinferenceModels}

import scala.util.boundary
import boundary.break
import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source
import scala.util.{Try, Using}

final case class ScafiProgram(program: String)

abstract class AbstractScafiProgramTest(
    private val knowledgePaths: List[String],
    private val promptsFilePath: String,

    private val loaders: List[CodeGeneratorService] = List(
      GeminiService.flash(GeminiService.Version.V1_5),
      GeminiService.flashExp(GeminiService.Version.V2_0),
      GeminiService.proExp(GeminiService.Version.V2_0),
    ),
    private val ollamaLoaders: List[OllamaModels] = OllamaModels.values.toList,
    private val openRouterModels: List[OpenRouterModels] = OpenRouterModels.values.toList,
    private val gitHubModels: List[GitHubModels] = GitHubModels.values.toList,
    private val xInferenceModels: List[XinferenceModels] = XinferenceModels.values.toList,
    private val geminiLangChainModels: List[LangChainModel] = List(
      GeminiLangChainModel(GeminiService.flash(GeminiService.Version.V1_5).apiKey, GeminiService.flash(GeminiService.Version.V1_5).model),
      GeminiLangChainModel(GeminiService.flashExp(GeminiService.Version.V2_0).apiKey, GeminiService.flashExp(GeminiService.Version.V2_0).model),
      GeminiLangChainModel(GeminiService.proExp(GeminiService.Version.V2_0).apiKey, GeminiService.proExp(GeminiService.Version.V2_0).model)
    ),
    private val runs: Int = 5,
    private val raw: Boolean = false,
):

  private lazy val candidatePrompts =
    decode[Prompts](Source.fromResource(promptsFilePath).mkString) match
      case Right(prompts) => prompts
      case Left(error) => throw new RuntimeException(s"Failed to decode prompts $error")

  private def programSpecification(
      knowledge: String,
      promptSpecification: String,
      model: CodeGeneratorService,
  ): ExecutionContext ?=> Future[ScafiProgram] =
    if !raw then model.generateMain(knowledge, promptSpecification).map(ScafiProgram(_))
    else model.generateRaw(knowledge, "", promptSpecification).map(ScafiProgram(_))


  private def executeScafiProgram(
      programUnderTest: ScafiProgram,
      preamble: String,
      post: String,
  ): Either[ScafiTestResult, Network] =
    val builtProgram = buildProgram(programUnderTest.program, preamble, post)
    val res = Try { executeFromString[Network](builtProgram) }.toEither.left.map(_ =>
      CompilationFailed(programUnderTest.program),
    )
    res

  def baselineWorkingProgram(): String

  def programTests(program: String, producedNet: Network): ScafiTestResult

  def postAction(): String = ""

  def preAction(): String = ""

  def testCase: String

  private def processLLMTest(n: Int, prompt: String, knowledgeFile: String, model: CodeGeneratorService)
                            (using ExecutionContext): Future[SingleTestResult] =
    Future(Using(Source.fromResource(knowledgeFile))(_.mkString).toEither)
      .flatMap:
        case Left(error) => Future.successful(SingleTestResult(testCase, n, knowledgeFile, model.toString, GenericFailure(error.getMessage)))
        case Right(knowledge) => processTest(n, prompt, knowledge, model)

  private def processRAGTest(n: Int, prompt: String, knowledgeFile: String, rag: CodeGeneratorService)
                            (using ExecutionContext): Future[SingleTestResult] =
    processTest(n, prompt, knowledgeFile, rag)

  private def processTest(n: Int, prompt: String, knowledge: String, model: CodeGeneratorService)
                         (using ExecutionContext): Future[SingleTestResult] =
    programSpecification(knowledge, prompt, model).map: currentProgram =>
      val result = executeScafiProgram(currentProgram, preAction(), postAction())
        .map(programTests(currentProgram.program, _))
        .fold(identity, identity)

      SingleTestResult(testCase, n, knowledge, model.toString, result)


  private def selectTestLLM(n: Int,
                         prompt: String,
                         knowledgeFile: String,
                         modelOpt: Option[CodeGeneratorService],
                         openRouterModelOpt: Option[OpenRouterModels],
                         langChainModel: Option[LangChainModel],
                         subMethod: String)
                        (using ExecutionContext): Future[SingleTestResult] =

    val model = subMethod match
      case "API REST" => modelOpt.getOrElse(throw new IllegalArgumentException("Missing model for API REST"))
      case "LANGCHAIN" => LangChainService(langChainModel.get)
      case "OPENROUTER" => OpenRouterService(openRouterModelOpt.get)
      case _ => throw new IllegalArgumentException("Invalid LLM subMethod")

    println(s"Created model: ${model.toString}")
    processLLMTest(n, prompt, knowledgeFile, model)


  private def selectTestRAG(n:Int,
                            prompt: String,
                            model: LangChainModel,
                            knowledgeFile: String)
                           (using ExecutionContext): Future[SingleTestResult] =
    val ragService = new RAGService(model, knowledgeFile)
    println(s"RAG: ${ragService.toString}")
    processRAGTest(n, prompt, knowledgeFile, ragService)


  def executeTest(method: String, subMethod: String, langChainType: String, ngrokAddress: String): ExecutionContext ?=> Seq[Future[SingleTestResult]] =
    boundary:
      // Execute baseline test
      val baselineProgram = ScafiProgram(baselineWorkingProgram())
      val baselineResult = executeScafiProgram(baselineProgram, preAction(), postAction())
      if baselineResult.isLeft then
        println(s"Failed to compile baseline program: ${baselineResult.left}")
        break(Seq())

      val baselineNetwork = baselineResult.getOrElse(throw new RuntimeException("Baseline program failed to compile"))
      val baselineTestResult = programTests(baselineProgram.program, baselineNetwork)
      if !baselineTestResult.isInstanceOf[ScafiTestResult.Success] then
        println(s"Baseline test failed: $baselineTestResult")
        break(Seq())

      // Execute LLM or RAG tests
      executeTests(method, subMethod, langChainType, ngrokAddress)

  private def executeTests(
                            method: String,
                            subMethod: String,
                            langChainType: String,
                            ngrokAddress: String
                          ): ExecutionContext ?=> Seq[Future[SingleTestResult]] =

    val testCases = for
      n <- 0 until runs
      prompt <- candidatePrompts.prompts
      knowledgeFile <- knowledgePaths
      result <- (method, subMethod, langChainType) match
        case ("RAG", "OLLAMA", _) =>
          ollamaLoaders.map(ollamaModel =>
            selectTestRAG(n, prompt, OllamaLangChainModel(ollamaModel.toString, ngrokAddress), knowledgeFile)
          )
        case ("RAG", _, _) =>
          geminiLangChainModels.map(geminiModel =>
            selectTestRAG(n, prompt, geminiModel, knowledgeFile)
          )
        case (_, "OPENROUTER", _) =>
          openRouterModels.map(openRouterModel =>
            selectTestLLM(n, prompt, knowledgeFile, None, Some(openRouterModel), None, subMethod)
          )
        case (_, "LANGCHAIN", "OLLAMA") =>
          ollamaLoaders.map(ollamaModel =>
            selectTestLLM(n, prompt, knowledgeFile, None, None, Some(OllamaLangChainModel(ollamaModel.toString, ngrokAddress)), subMethod)
          )
        case (_, "LANGCHAIN", "LOCALAI") =>
          ollamaLoaders.map(ollamaModel =>
            selectTestLLM(n, prompt, knowledgeFile, None, None, Some(LocalAiLangChainModel(ollamaModel.toString, ngrokAddress)), subMethod)
          )
        case (_, "LANGCHAIN", "XINFERENCE") =>
          xInferenceModels.map(xInferenceModel =>
            selectTestLLM(n, prompt, knowledgeFile, None, None, Some(XinferenceLangChainModel(xInferenceModel.toString, ngrokAddress)), subMethod)
          )
        case (_, "LANGCHAIN", "GEMINI") =>
          geminiLangChainModels.map(geminiModel =>
            selectTestLLM(n, prompt, knowledgeFile, None, None, Some(geminiModel), subMethod)
          )
        case (_, "LANGCHAIN", "GITHUB") =>
          gitHubModels.map(gitHubModel =>
            selectTestLLM(n, prompt, knowledgeFile, None, None, Some(GitHubLangChainModel(System.getenv("GITHUB_TOKEN"), gitHubModel.toString)), subMethod)
          )
        case _ =>
          loaders.map(model =>
            selectTestLLM(n, prompt, knowledgeFile, Some(model), None, None, subMethod)
          )
    yield result

    testCases

end AbstractScafiProgramTest
