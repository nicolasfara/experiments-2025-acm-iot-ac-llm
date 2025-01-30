package it.unibo.scafi.tests.bok

import scala.language.postfixOps
import it.unibo.scafi.FunctionalTestIncarnation.Network
import it.unibo.scafi.ScafiAssertions.assertNetworkValues
import it.unibo.scafi.AbstractScafiProgramTest
import org.scalatest.Assertion

class NeighboringMinDistanceTest extends AbstractScafiProgramTest(List("knowledge/knowledge.md"), "prompts/NeighboringMinDistanceTest.json"):
  override def testCase: String = "calculate the min distance from neighbors, in a grid"
  override def baselineWorkingProgram(): String =
    """
      foldhood (Double.MaxValue) ((x,y)=>if (x<y) x else y) {
        mux(mid()==nbr(mid())) { Double.MaxValue }{ nbrvar[Double](NBR_RANGE) }
      }
    """

  override def programTests(producedNet: Network): Assertion =
      assertNetworkValues((0 to 8).zip(List(1, 1, 1, 1, 1, 1, 1, 1, 1)).toMap)(producedNet)
