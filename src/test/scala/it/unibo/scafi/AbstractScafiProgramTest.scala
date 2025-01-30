package it.unibo.scafi

import scala.io.Source
import it.unibo.scafi.FunctionalTestIncarnation.Network
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import it.unibo.scafi.ScafiTestUtils.*
import io.circe.parser.*
import io.circe.generic.auto.*
import org.scalatest.Assertion

import scala.concurrent.Future
import scala.util.{ Failure, Success, Try, Using }
import scala.jdk.CollectionConverters.*

final case class ScafiProgram(program: String)

abstract class AbstractScafiProgramTest(
    private val knowledgePaths: List[String],
    private val promptsFilePath: String,
    private val loader: CodeGeneratorService = GeminiService.flash(GeminiService.Version.V2_0),
    private val runs: Int = 5,
    private val raw: Boolean = false,
) extends AsyncFlatSpec,
      Matchers:

  private lazy val candidatePrompts =
    decode[Prompts](Source.fromResource(promptsFilePath).mkString) match
      case Right(prompts) => prompts
      case Left(error) => throw new RuntimeException(s"Failed to decode prompts $error")

  private def programSpecification(knowledge: String, promptSpecification: String): Future[ScafiProgram] = if !raw then
    loader.generateMain(knowledge, promptSpecification).map(ScafiProgram(_))
  else loader.generateRaw(knowledge, "", promptSpecification).map(ScafiProgram(_))

  private def executeScafiProgram(programUnderTest: ScafiProgram, post: String): Network =
    Try { executeFromString[Network](programUnderTest.program, post = post) } match
      case Success(producedNet) => producedNet
      case Failure(exception) =>
        fail(s"Failed to execute program ${programUnderTest.program}", exception)

  def baselineWorkingProgram(): String

  def programTests(producedNet: Network): Assertion

  def postAction(): String = ""

  def testCase: String

  // Baseline verification from Scafi
  it should s"$testCase [synthetic test]" in:
    programTests(executeScafiProgram(ScafiProgram(baselineWorkingProgram()), postAction()))

  for
    n <- 0 until runs
    prompt <- candidatePrompts.prompts
    knowledgeFile <- knowledgePaths
  do
    Using(Source.fromResource(knowledgeFile)): source =>
      val knowledge = source.mkString
      // Check if the provided program is correct as the synthetic test program
      it should s"$testCase with knowledge $knowledgeFile @ round-$n-prompt${candidatePrompts.prompts.indexOf(prompt)}" in:
        programSpecification(knowledge, prompt).map(program => programTests(executeScafiProgram(program, postAction())))
end AbstractScafiProgramTest
