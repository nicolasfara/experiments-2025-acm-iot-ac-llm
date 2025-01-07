package it.unibo.scafi.tests

import it.unibo.scafi.FunctionalTestIncarnation.Network
import it.unibo.scafi.ScafiAssertions.assertNetworkValues
import it.unibo.scafi.{ AbstractScafiProgramTest, CodeGeneratorService }

import scala.language.postfixOps

private val neighboringTest = new CodeGeneratorService:
  override def localKnowledge: String = ""
  override def generateCode(prompt: String): String =
    """
      foldhood(0)(_ + _){1}
    """

private val neighborCountPrompts = List(
  "",
)

class NeighboringTest extends AbstractScafiProgramTest(neighborCountPrompts, neighboringTest):

  override def behaviors(producedNet: Network): Unit =
    it should "count neighbors" in:
      assertNetworkValues((0 to 8).zip(List(4, 6, 4, 6, 9, 6, 4, 6, 4)).toMap)
