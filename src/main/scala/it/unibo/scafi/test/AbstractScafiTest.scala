package it.unibo.scafi.test

import io.circe.generic.auto.*
import io.circe.parser.*
import it.unibo.scafi.test.ScafiTestUtils.{ buildProgram, executeFromString }
import it.unibo.scafi.test.FunctionalTestIncarnation.Network
import it.unibo.scafi.test.ScafiTestResult.{ CompilationFailed, GenericFailure }
import it.unibo.scafi.{ CodeGeneratorService, GeminiService, Prompts }

import scala.util.boundary
import boundary.break
import scala.concurrent.{ ExecutionContext, Future }
import scala.io.Source
import scala.util.{ Try, Using }

final case class ScafiProgram(program: String)

abstract class AbstractScafiProgramTest(
    private val knowledgePaths: List[String],
    private val promptsFilePath: String,
    private val loaders: List[CodeGeneratorService] = List(
      GeminiService.flash(GeminiService.Version.V1_5),
      GeminiService.flashExp(GeminiService.Version.V2_0),
      GeminiService.proExp(GeminiService.Version.V2_0),
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

  def executeTest(): ExecutionContext ?=> Seq[Future[SingleTestResult]] =
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
      // execute LLM tests
      for
        n <- 0 until runs
        prompt <- candidatePrompts.prompts
        knowledgeFile <- knowledgePaths
        model <- loaders
      yield Using(Source.fromResource(knowledgeFile))(_.mkString).toEither match
        case Left(error) =>
          Future(SingleTestResult(testCase, n, knowledgeFile, model.toString, GenericFailure(error.getMessage)))
        case Right(knowledge) =>
          programSpecification(knowledge, prompt, model).map: currentProgram =>
            val outcome =
              for producedNetwork <- executeScafiProgram(currentProgram, preAction(), postAction())
              yield programTests(currentProgram.program, producedNetwork)
            val result = outcome match
              case Right(value) => value
              case Left(error) => error
            SingleTestResult(testCase, n, knowledgeFile, model.toString, result)
end AbstractScafiProgramTest
