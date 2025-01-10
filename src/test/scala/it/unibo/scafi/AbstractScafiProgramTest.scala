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
import scala.util.{Failure, Success, Try}

final case class ScafiProgram(program: String)

abstract class AbstractScafiProgramTest(
    private val promptsFilePath: String,
    private val loader: CodeGeneratorService = GeminiService.flash(GeminiService.Version.V2_0),
    private val runs: Int = 5,
) extends AsyncFlatSpec,
      Matchers:

  private lazy val candidatePrompts =
    decode[Prompts](Source.fromResource(promptsFilePath).mkString) match
      case Right(prompts) => prompts
      case Left(error) => throw new RuntimeException(s"Failed to decode prompts $error")

  private def programSpecification(promptSpecification: String): Future[ScafiProgram] =
    loader.generateCode(promptSpecification).map(ScafiProgram(_))

  private def executeScafiProgram(programUnderTest: ScafiProgram): Network =
    Try { executeFromString[Network](programUnderTest.program) } match
      case Success(producedNet) => producedNet
      case Failure(exception) =>
        fail(s"Failed to execute program ${programUnderTest.program}", exception)

  def baselineWorkingProgram(): String

  def programTests(producedNet: Network): Assertion

  def testCase: String

  // Baseline verification from Scafi
  it should s"$testCase [synthetic test]" in:
    programTests(executeScafiProgram(ScafiProgram(baselineWorkingProgram())))

  for
    n <- 0 until runs
    prompt <- candidatePrompts.prompts
  do
    // Check if the provided program is correct as the synthetic test program
    it should s"$testCase @ round-$n-prompt${candidatePrompts.prompts.indexOf(prompt)}" in:
      programSpecification(prompt).map(program => programTests(executeScafiProgram(program)))
end AbstractScafiProgramTest
