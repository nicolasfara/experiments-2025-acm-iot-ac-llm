package it.unibo.scafi

import scala.io.Source

import it.unibo.scafi.FunctionalTestIncarnation.Network
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import it.unibo.scafi.ScafiTestUtils.*
import io.circe.parser.*
import io.circe.generic.auto.*

final case class ScafiProgram(program: String)

abstract class AbstractScafiProgramTest(
    private val promptsFilePath: String,
    private val loader: CodeGeneratorService =
      GeminiService.flash(GeminiService.Version.V2_0, System.getenv("API_KEY"), ""),
    private val runs: Int = 5,
) extends AnyFlatSpec,
      Matchers:

  private lazy val candidatePrompts =
    decode[Prompts](Source.fromResource(promptsFilePath).getLines().mkString) match
      case Right(prompts) => prompts
      case Left(error) => throw new RuntimeException(s"Failed to load prompts $error")

  private def programSpecification(promptSpecification: String): ScafiProgram =
    ScafiProgram(loader.generateCode(promptSpecification))

  private def executeScafiProgram(programUnderTest: ScafiProgram): Network =
    executeFromString(programUnderTest.program)

  def baselineWorkingProgram(): String

  def programTests(producedNet: Network): Unit

  // Baseline verification from Scafi
  behavior of "synthetic test program"
  it should behave like programTests(executeScafiProgram(ScafiProgram(baselineWorkingProgram())))

  for
    n <- 0 until runs
    prompt <- candidatePrompts.prompts
    program = programSpecification(prompt)
  do
    // Check if the provided program is correct as the synthetic test program
    behavior of s"${this.getClass.getSimpleName} @ round-$n-prompt${candidatePrompts.prompts.indexOf(prompt)}"
    it should behave like programTests(executeScafiProgram(program))
end AbstractScafiProgramTest
