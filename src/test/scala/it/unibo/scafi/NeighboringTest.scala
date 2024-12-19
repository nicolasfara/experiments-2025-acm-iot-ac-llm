package it.unibo.scafi

import it.unibo.scafi.FunctionalTestIncarnation.Network
import it.unibo.scafi.ScafiAssertions.assertNetworkValues

import scala.language.postfixOps

val nbrLoader = new ProgramLoader:
  override def loadProgram(prompt: String): String =
    """
      foldhood(0)(_ + _){if (nbr[Int](mid())==mid()) 0 else 1}
    """

class NeighboringTest extends AbstractScafiProgramTest(
  PromptSpecification(List(""), "nbr test"),
  nbrLoader,
):
  override def behaviors(producedNet: Network): Unit =
    it should "pippo" in:
      assertNetworkValues((0 to 8).zip(List(3,5,3,5,8,5,3,5,3)).toMap)(producedNet)
