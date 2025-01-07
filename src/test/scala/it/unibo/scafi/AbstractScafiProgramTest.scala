package it.unibo.scafi

import it.unibo.scafi.FunctionalTestIncarnation.Network
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import it.unibo.scafi.ScafiTestUtils.*

final case class ScafiProgram(program: String)

abstract class AbstractScafiProgramTest(
    private val promptSpecification: List[String],
    private val loader: CodeGeneratorService,
    private val runs: Int = 5,
) extends AnyFlatSpec,
      Matchers:

  private def programSpecification(promptSpecification: String): ScafiProgram =
    ScafiProgram(loader.generateCode(promptSpecification))

  private def executeScafiProgram(programUnderTest: ScafiProgram): Network =
    executeFromString(programUnderTest.program)

  def behaviors(producedNet: Network): Unit

  for
    n <- 0 until runs
    prompt <- promptSpecification
    program = programSpecification(prompt)
  do
    behavior of s"${this.getClass.getSimpleName} @ round-$n-prompt${promptSpecification.indexOf(prompt)}"
    it should behave like behaviors(executeScafiProgram(program))
end AbstractScafiProgramTest
