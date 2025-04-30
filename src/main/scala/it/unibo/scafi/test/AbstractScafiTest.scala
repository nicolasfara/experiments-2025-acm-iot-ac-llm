package it.unibo.scafi.test

import scala.concurrent.{ ExecutionContext, Future }
import scala.io.Source
import scala.util.boundary.break
import scala.util.{ boundary, Try, Using }
import io.circe.generic.auto.*
import io.circe.parser.*
import it.unibo.scafi.Prompts
import it.unibo.scafi.program.llm.*
import it.unibo.scafi.program.utils.PromptUtils.generatePreamblePrompt
import it.unibo.scafi.test.FunctionalTestIncarnation.Network
import it.unibo.scafi.test.ScafiTestResult.{ CompilationFailed, GenericFailure }
import it.unibo.scafi.test.ScafiTestUtils.{ buildProgram, executeFromString }
import org.slf4j.LoggerFactory

final case class ScafiProgram(program: String)

abstract class AbstractScafiProgramTest(
    private val knowledgePaths: List[String],
    private val promptsFilePath: String,
    private val loaders: List[CodeGeneratorService] = List(
//      OpenRouterService(Model.GEMMA_3_4B),
//      OpenRouterService(Model.GEMMA_3_12B),
      OpenRouterService(Model.GEMMA_3_27B),
//      GeminiService(Model.GEMINI_2_5_PRO),
//      GeminiService(Model.GEMINI_2_FLASH_EXP),
//      GeminiService(Model.GEMINI_1_5_FLASH),
//      OpenRouterService(Model.LLAMA_3_3_70B_INSTRUCT),
//      OpenRouterService(Model.LLAMA_4_SCOUT),
//      OpenRouterService(Model.MISTRAL_SMALL_3_1_24B),
//      OpenRouterService(Model.MISTRAL_8B),
//      OpenRouterService(Model.QWEN_2_5_CODER_32B),
//      OpenRouterService(Model.DEEPSEEK_R1),
//      OpenRouterService(Model.GPT_4_1_MINI),
    ),
    private val runs: Int = 5,
):
  private val logger = LoggerFactory.getLogger(this.getClass)
  private lazy val candidatePrompts =
    decode[Prompts](Source.fromResource(promptsFilePath).mkString) match
      case Right(prompts) => prompts
      case Left(error) =>
        logger.error(s"Failed to decode prompts: $error")
        throw new RuntimeException(s"Failed to decode prompts $error")

  private def programSpecification(
      knowledge: String,
      promptSpecification: String,
      model: CodeGeneratorService,
  ): ExecutionContext ?=> Future[ScafiProgram] =
    model.generateRaw(knowledge, generatePreamblePrompt(), promptSpecification).map(ScafiProgram(_))

  private def executeScafiProgram(
      programUnderTest: ScafiProgram,
      preamble: String,
      post: String,
  )(using ExecutionContext): Either[ScafiTestResult, Network] =
    val builtProgram = buildProgram(programUnderTest.program, preamble, post)
    logger.info(s"Starting execution of the test case: `$testCase``")
    val res = Try:
      executeFromString[Network](builtProgram)
    .toEither.left.map: e =>
      logger.error(
        s"""Failed to execute test case `$testCase` with error: ${e.getMessage}
           |
           |Generated program:
           |${programUnderTest.program}
           |""".stripMargin,
      )
      CompilationFailed("<redacted for size reasons, see logs>")
    logger.info(s"Finalized `$testCase`")
    res
  end executeScafiProgram

  def baselineWorkingProgram(): String

  def programTests(program: String, producedNet: Network): ScafiTestResult

  def postAction(): String = ""

  def preAction(): String = ""

  def testCase: String

  def executeTest(): ExecutionContext ?=> Seq[Future[SingleTestResult]] =
    boundary:
      // Execute baseline test
      val baselineProgram = ScafiProgram(baselineWorkingProgram())
      val baselineResult = executeScafiProgram(baselineProgram, preAction(), postAction())
      if baselineResult.isLeft then
        logger.error(s"Failed to compile baseline program for $testCase: ${baselineResult.left}")
        break(Seq())

      val baselineNetwork = baselineResult.getOrElse(throw new RuntimeException("Baseline program failed to compile"))
      val baselineTestResult = programTests(baselineProgram.program, baselineNetwork)
      if !baselineTestResult.isInstanceOf[ScafiTestResult.Success] then
        logger.error(s"Baseline test failed: $baselineTestResult")
        break(Seq())
      // execute LLM tests
      for
        n <- 0 until runs
        prompt <- candidatePrompts.prompts
        knowledgeFile <- knowledgePaths
        model <- loaders
      yield Using(Source.fromResource(knowledgeFile))(_.mkString).toEither match
        case Left(error) =>
          logger.error(s"Failed to load knowledge file $knowledgeFile: ${error.getMessage}")
          Future(SingleTestResult(testCase, n, knowledgeFile, model.toString, GenericFailure(error.getMessage)))
        case Right(knowledge) =>
          programSpecification(knowledge, prompt, model).map: currentProgram =>
            val outcome =
              for producedNetwork <- executeScafiProgram(currentProgram, preAction(), postAction())
              yield programTests(currentProgram.program, producedNetwork)
            val result = outcome match
              case Right(value) =>
                logger.info(s"Program executed successfully with model $model: $value")
                value
              case Left(error) =>
                logger.error(s"Program execution failed with model $model: $error")
                error
            logger.info(s"Test case `$testCase` with model `$model` completed")
            SingleTestResult(testCase, n, knowledgeFile, model.toString, result)
      end for
end AbstractScafiProgramTest
