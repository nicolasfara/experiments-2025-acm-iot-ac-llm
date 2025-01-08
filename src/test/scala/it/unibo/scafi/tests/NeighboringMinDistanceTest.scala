package it.unibo.scafi.tests

import scala.language.postfixOps

import it.unibo.scafi.FunctionalTestIncarnation.Network
import it.unibo.scafi.ScafiAssertions.assertNetworkValues
import it.unibo.scafi.AbstractScafiProgramTest

class NeighboringMinDistanceTest extends AbstractScafiProgramTest(s"prompts/NeighboringMinDistanceTest.json"):

  override def baselineWorkingProgram(): String =
    """
      foldhood (Double.MaxValue) ((x,y)=>if (x<y) x else y) {
        mux(mid()==nbr(mid())) { Double.MaxValue }{ nbrvar[Double](NBR_RANGE) }
      }
    """

  override def programTests(producedNet: Network): Unit =
    it should "calculate the min distance from neighbors, in a grid" in:
      assertNetworkValues((0 to 8).zip(List(1, 1, 1, 1, 1, 1, 1, 1, 1)).toMap)(producedNet)
