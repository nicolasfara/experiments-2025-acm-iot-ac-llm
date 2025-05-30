package it.unibo.scafi.test

import scala.io.{ BufferedSource, Source }

import io.circe.generic.auto.*
import io.circe.parser.*
import it.unibo.scafi.program.llm.*
import it.unibo.scafi.program.utils.PromptUtils.generatePreamblePrompt
import it.unibo.scafi.test.FunctionalTestIncarnation.Network
import it.unibo.scafi.test.ScafiTestUtils.{ buildProgram, executeFromString }
import org.typelevel.log4cats.*
import cats.effect.*
import cats.effect.syntax.all.*
import org.typelevel.log4cats.slf4j.Slf4jFactory

final case class ScafiProgram(program: String)

final case class Prompts(prompts: List[String])

abstract class AbstractScafiProgramTest(
    private val knowledgePaths: List[String],
    private val promptsFilePath: String,
    private val loaders: List[CodeGeneratorService] = List(
      OpenRouterService(Model.GEMMA_3_4B),
      OpenRouterService(Model.GEMMA_3_12B),
      OpenRouterService(Model.GEMMA_3_27B),
      GeminiService(Model.GEMINI_2_5_PRO),
      GeminiService(Model.GEMINI_2_FLASH),
      GeminiService(Model.GEMINI_1_5_FLASH),
      OpenRouterService(Model.LLAMA_3_3_70B_INSTRUCT),
      OpenRouterService(Model.LLAMA_4_SCOUT),
      OpenRouterService(Model.MISTRAL_SMALL_3_1_24B),
      OpenRouterService(Model.MISTRAL_8B),
      OpenRouterService(Model.QWEN_2_5_CODER_32B),
//      //      OpenRouterService(Model.DEEPSEEK_R1), // NOT WORKING !
      OpenRouterService(Model.GPT_4_1_MINI),
      FileBasedReplayer("claude-3-7-sonnet"),
      FileBasedReplayer("meta_llama-3.1-405b-instruct-maas"),
      FileBasedReplayer("meta_llama-4-maverick-17b-128e-instruct-maas"),
      FileBasedReplayer("codestral-2501"),
    ),
    private val runs: Int = 20,
):
  private lazy val candidatePrompts =
    decode[Prompts](Source.fromResource(promptsFilePath).mkString) match
      case Right(prompts) => prompts
      case Left(error) => throw new RuntimeException(s"Failed to decode prompts $error")

  given LoggerFactory[IO] = Slf4jFactory.create[IO]
  val logger: SelfAwareStructuredLogger[IO] = LoggerFactory[IO].getLogger

  private def programSpecification(
      knowledge: String,
      promptSpecification: String,
      model: CodeGeneratorService,
  ): IO[ScafiProgram] =
    model.generateRaw(knowledge, generatePreamblePrompt(), promptSpecification).map(ScafiProgram(_))

  private def executeScafiProgram(
      programUnderTest: ScafiProgram,
      preamble: String,
      post: String,
  ): IO[Network] =
    val builtProgram = buildProgram(programUnderTest.program, preamble, post)
    executeFromString[Network](builtProgram)

  def baselineWorkingProgram(): String

  def programTests(program: String, producedNet: Network): ScafiTestResult

  def postAction(): String = ""

  def preAction(): String = ""

  def testCase: String

  private def getSource(fileName: String): IO[BufferedSource] = IO(Source.fromResource(fileName))

  private def readFile(src: Source): IO[String] = IO(src.getLines.mkString)

  private def closeSource(src: Source): IO[Unit] = IO(src.close)

  private def openKnowledgeFile(file: String): Resource[IO, BufferedSource] =
    Resource.make(getSource(file))(f => closeSource(f))

  extension (computation: IO[ScafiTestResult])
    private def mapError(program: String): IO[ScafiTestResult] =
      computation
        .onError:
          case _: ScafiCompilationException =>
            logger.error(s"Failed to compile program: $program")
        .recover:
          case _: ScafiCompilationException => ScafiTestResult.CompilationFailed(program)

  def executeTest(): Seq[IO[SingleTestResult]] =
    val baselineProgram = ScafiProgram(baselineWorkingProgram())
    val baselineResult = logger.info(s"Starting baseline program execution: $testCase") >>
      executeScafiProgram(baselineProgram, preAction(), postAction())
        .map(programTests(baselineProgram.program, _))
        .mapError(baselineProgram.program)
        .map(SingleTestResult(testCase, 0, "baseline", "baseline", _)) <*
      logger.info(s"Baseline program execution completed: $testCase")
    // Specify the tests to be run
    val otherTests = for
      n <- 0 until runs
      prompt <- candidatePrompts.prompts
      knowledgeFile <- knowledgePaths
      model <- loaders
    yield openKnowledgeFile(knowledgeFile).use { knowledgeSource =>
      for
        _ <- logger.info(s"Starting test for `$testCase`@$n with `$knowledgeFile` and `$model`")
        knowledge <- readFile(knowledgeSource)
        _ <- logger.info("Knowledge loaded successfully")
        program <- programSpecification(knowledge, prompt, model)
        testResult <- executeScafiProgram(program, preAction(), postAction())
          .map(programTests(program.program, _))
          .mapError(program.program)
        _ <- logger.info(s"Test for `$testCase`@$n with `$knowledgeFile` and `$model` completed")
      yield SingleTestResult(testCase, n, knowledgeFile, model.toString, testResult)
    }
    baselineResult +: otherTests
  end executeTest
end AbstractScafiProgramTest
