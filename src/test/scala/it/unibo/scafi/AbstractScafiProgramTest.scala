package it.unibo.scafi

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import it.unibo.scafi.platform.SimulationPlatform

final case class ScafiProgram(program: String)
final case class PromptSpecification(prompts: List[String], testCase: String)

abstract class AbstractScafiProgramTest(
    private val promptSpecification: PromptSpecification,
    private val loader: ProgramLoader,
    private val runs: Int = 5,
) extends AnyFlatSpec,
      Matchers:

  private def programSpecification(promptSpecification: String): ScafiProgram =
    ScafiProgram(loader.loadProgram(promptSpecification))

  private def executeScafiProgram(programUnderTest: ScafiProgram): SimulationPlatform#Network =
    dotty.tools.repl
      .ScriptEngine()
      .eval(
        s"""
         |import it.unibo.scafi.ScafiTestUtils.*
         |import it.unibo.scafi.platform.SimulationPlatform
         |import it.unibo.scafi.config.GridSettings
         |import it.unibo.scafi.incarnations.BasicSimulationIncarnation.{
         |  simulatorFactory,
         |  BasicAggregateInterpreter,
         |  Seeds,
         |  SimulatorOps,
         |  StandardSensorNames,
         |}
         |import it.unibo.scafi.core.FunctionalTestIncarnation
         |
         |val net: SimulationPlatform#Network & SimulatorOps =
         |  simulatorFactory.gridLike(GridSettings(3, 3, 1, 1), rng = 1.5, seeds = Seeds(1, 1, 1))
         |
         |given node: (BasicAggregateInterpreter & StandardSensorNames) = new BasicAggregateInterpreter with StandardSensorNames
         |
         |runProgram {
         |  ${programUnderTest.program}
         |}(net)(using node)
         |
         |""".stripMargin,
      )
    ???
  end executeScafiProgram

  def testProgram(producedNet: SimulationPlatform#Network): Unit

  for
    run <- 0 until runs
    prompt <- promptSpecification.prompts
    program = programSpecification(prompt)
  do it should behave like testProgram(executeScafiProgram(program))
end AbstractScafiProgramTest
