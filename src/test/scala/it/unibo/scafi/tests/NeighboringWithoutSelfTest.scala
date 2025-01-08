package it.unibo.scafi.tests

import scala.language.postfixOps

import it.unibo.scafi.FunctionalTestIncarnation.Network
import it.unibo.scafi.ScafiAssertions.assertNetworkValues
import it.unibo.scafi.{ AbstractScafiProgramTest, CodeGeneratorService }

private val neighboringWithoutSelfTest = new CodeGeneratorService:
  override def localKnowledge: String = ""
  override def generateCode(prompt: String): String =
    """
      foldhood(0)(_ + _){if (nbr[Int](mid())==mid()) 0 else 1}
    """

private val neighborCountPrompts = List(
  "",
)

class NeighboringWithoutSelfTest extends AbstractScafiProgramTest(neighborCountPrompts, neighboringWithoutSelfTest):

  override def behaviors(producedNet: Network): Unit =
    it should "count neighbors excluding self" in:
      assertNetworkValues((0 to 8).zip(List(3, 5, 3, 5, 8, 5, 3, 5, 3)).toMap)(producedNet)
