package it.unibo.scafi.tests

import it.unibo.scafi.FunctionalTestIncarnation.Network
import it.unibo.scafi.ScafiAssertions.assertNetworkValues
import it.unibo.scafi.{ AbstractScafiProgramTest, CodeGeneratorService }

import scala.language.postfixOps

private val neighboringMinDistanceTest = new CodeGeneratorService:
  override def localKnowledge: String = ""
  override def generateCode(prompt: String): String =
    """
      foldhood (Double.MaxValue) ((x,y)=>if (x<y) x else y) {
        mux(mid()==nbr(mid())) { Double.MaxValue }{ nbrvar[Double](NBR_RANGE) }
      }
    """

private val neighborCountPrompts = List(
  "",
)

class NeighboringMinDistanceTest
    extends AbstractScafiProgramTest(
      neighborCountPrompts,
      neighboringMinDistanceTest,
    ):

  override def behaviors(producedNet: Network): Unit =
    it should "calculate the min distance from neighbors, in a grid" in:
      assertNetworkValues((0 to 8).zip(List(1, 1, 1, 1, 1, 1, 1, 1, 1)).toMap)(producedNet)
