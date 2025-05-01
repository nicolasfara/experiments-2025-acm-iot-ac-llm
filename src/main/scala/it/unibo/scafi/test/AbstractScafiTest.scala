package it.unibo.scafi.test

import scala.io.{ BufferedSource, Source }
import io.circe.generic.auto.*
import io.circe.parser.*
import it.unibo.scafi.Prompts
import it.unibo.scafi.program.llm.*
import it.unibo.scafi.program.utils.PromptUtils.generatePreamblePrompt
import it.unibo.scafi.test.FunctionalTestIncarnation.Network
import it.unibo.scafi.test.ScafiTestUtils.{ buildProgram, executeFromString }
import org.typelevel.log4cats.*
import cats.effect.*
import cats.effect.syntax.all.*
import org.typelevel.log4cats.slf4j.Slf4jFactory

final case class ScafiProgram(program: String)

abstract class AbstractScafiProgramTest(
    private val knowledgePaths: List[String],
    private val promptsFilePath: String,
    private val loaders: List[CodeGeneratorService] = List(
//      OpenRouterService(Model.GEMMA_3_4B),
//      OpenRouterService(Model.GEMMA_3_12B),
//      OpenRouterService(Model.GEMMA_3_27B),
//      GeminiService(Model.GEMINI_2_5_PRO),
//      GeminiService(Model.GEMINI_2_FLASH),
//      GeminiService(Model.GEMINI_1_5_FLASH),
//      OpenRouterService(Model.LLAMA_3_3_70B_INSTRUCT),
//      OpenRouterService(Model.LLAMA_4_SCOUT),
//      OpenRouterService(Model.MISTRAL_SMALL_3_1_24B),
//      OpenRouterService(Model.MISTRAL_8B),
//      OpenRouterService(Model.QWEN_2_5_CODER_32B),
      OpenRouterService(Model.DEEPSEEK_R1),
//      OpenRouterService(Model.GPT_4_1_MINI),
    ),
    private val runs: Int = 20,
):
//  private val logger = LoggerFactory.getLogger(this.getClass)
  private lazy val candidatePrompts =
    decode[Prompts](Source.fromResource(promptsFilePath).mkString) match
      case Right(prompts) => prompts
      case Left(error) =>
//        logger.error(s"Failed to decode prompts: $error")
        throw new RuntimeException(s"Failed to decode prompts $error")

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

  def getSource(fileName: String): IO[BufferedSource] = IO(Source.fromResource(fileName))

  def readFile(src: Source): IO[String] = IO(src.getLines.mkString)

  def closeSource(src: Source): IO[Unit] = IO(src.close)

  private def openKnowledgeFile(file: String): Resource[IO, BufferedSource] =
    Resource.make(getSource(file))(f => closeSource(f))

  def executeTest(): Seq[IO[SingleTestResult]] =
    val baselineProgram = ScafiProgram(baselineWorkingProgram())
    val baselineResult =
      logger.info(s"Starting baseline program execution: $testCase") >>
        executeScafiProgram(baselineProgram, preAction(), postAction())
          .map(programTests(baselineProgram.program, _))
          .onError:
            case _: ScafiCompilationException =>
              logger.error(s"Failed to compile baseline program: ${baselineProgram.program}")
          .recover:
            case _: ScafiCompilationException => ScafiTestResult.CompilationFailed(baselineProgram.program)
          .map(SingleTestResult(testCase, 0, "baseline", "baseline", _)) <*
        logger.info(s"Baseline program execution completed: $testCase")
    val otherTests = for
      n <- 0 until runs
      prompt <- candidatePrompts.prompts
      knowledgeFile <- knowledgePaths
      model <- loaders
    yield openKnowledgeFile(knowledgeFile).use { knowledgeSource =>
      for
        _ <- logger.info(s"Starting test for `$testCase`@$n with `$knowledgeFile` and `$model`")
        knowledge <- readFile(knowledgeSource)
        _ <- logger.info(s"Knowledge loaded successfully")
        program <- programSpecification(knowledge, prompt, model)
        testResult <- executeScafiProgram(program, preAction(), postAction())
          .map(programTests(program.program, _))
          .onError:
            case _: ScafiCompilationException =>
              logger.error(s"Failed to compile@$n with `$knowledgeFile` and `$model`: ${program.program}")
          .recover:
            case _: ScafiCompilationException => ScafiTestResult.CompilationFailed(baselineProgram.program)
        _ <- logger.info(s"Test for `$testCase`@$n with `$knowledgeFile` and `$model` completed")
      yield SingleTestResult(testCase, n, knowledgeFile, model.toString, testResult)
    }
    baselineResult +: otherTests
  end executeTest
end AbstractScafiProgramTest
