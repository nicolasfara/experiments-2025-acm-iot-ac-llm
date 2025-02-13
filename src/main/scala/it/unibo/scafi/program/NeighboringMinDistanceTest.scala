package it.unibo.scafi.program

import it.unibo.scafi.test.ScafiAssertions.assertNetworkValues
import it.unibo.scafi.test.{ AbstractScafiProgramTest, FunctionalTestIncarnation, ScafiTestResult }

class NeighboringMinDistanceTest
    extends AbstractScafiProgramTest(
      List("knowledge/knowledge-with-building-blocks.md"),
      "prompts/NeighboringMinDistanceTest.json",
    ):
  override def testCase: String = "calculate the min distance from neighbors, in a grid"
  override def baselineWorkingProgram(): String =
    """
      foldhood (Double.MaxValue) ((x,y)=>if (x<y) x else y) {
        mux(mid()==nbr(mid())) { Double.MaxValue }{ nbrvar[Double](NBR_RANGE) }
      }
    """

  override def programTests(program: String, producedNet: FunctionalTestIncarnation.Network): ScafiTestResult =
    assertNetworkValues(program, (0 to 8).zip(List(1, 1, 1, 1, 1, 1, 1, 1, 1)).toMap)(producedNet)
