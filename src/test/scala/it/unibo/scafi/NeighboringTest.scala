package it.unibo.scafi

import it.unibo.scafi.platform.SimulationPlatform

import scala.language.postfixOps

val nbrLoader = new ProgramLoader:
  override def loadProgram(prompt: String): String =
    """
      |nbr(1)
      |""".stripMargin

class NeighboringTest extends AbstractScafiProgramTest(
  PromptSpecification(List(""), "nbr test"),
  nbrLoader,
):
  override def testProgram(producedNet: SimulationPlatform#Network): Unit =
    it should "pippo" in:
      ???
