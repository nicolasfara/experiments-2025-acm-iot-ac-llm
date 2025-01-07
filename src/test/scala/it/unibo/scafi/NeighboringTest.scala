package it.unibo.scafi

import it.unibo.scafi.FunctionalTestIncarnation.Network
import it.unibo.scafi.ScafiAssertions.assertNetworkValues

import scala.language.postfixOps

private val nbrLoader = new CodeGeneratorService:
  override def localKnowledge: String = ""
  override def generateCode(prompt: String): String =
    """
      foldhood(0)(_ + _){if (nbr[Int](mid())==mid()) 0 else 1}
    """

private val neighborCountPrompts = List(
  "",
)

class NeighboringTest
    extends AbstractScafiProgramTest(PromptSpecification(neighborCountPrompts, "nbr test"), nbrLoader):

  override def behaviors(producedNet: Network): Unit =
    it should "count the number of neighbors [predefined]" in:
      assertNetworkValues((0 to 8).zip(List(3, 5, 3, 5, 8, 5, 3, 5, 3)).toMap)(producedNet)
