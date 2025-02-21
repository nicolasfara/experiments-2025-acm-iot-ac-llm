package it.unibo.scafi.program

import it.unibo.scafi.test.ScafiAssertions.assertNetworkValues
import it.unibo.scafi.test.{ AbstractScafiProgramTest, FunctionalTestIncarnation, ScafiTestResult }

class GradientWithDistanceFromSourceAndObstacles
    extends AbstractScafiProgramTest(
      List("knowledge/knowledge-with-building-blocks.md"),
      "prompts/GradientWithDistanceFromSourceWithObstacles.json",
    ):
  override def testCase: String = "calculate the gradient (with obstacles) with distance from source"
  override def baselineWorkingProgram(): String =
    """
    rep(Double.PositiveInfinity) { case d =>
      branch(sense[Boolean]("obstacle")) {
        Double.PositiveInfinity
      } {
        mux(sense[Boolean]("source")) {
          0.0
        } {
          minHoodPlus(nbr(d) + nbrRange())
        }
      }
    }
    """.stripMargin

  override def programTests(program: String, producedNet: FunctionalTestIncarnation.Network): ScafiTestResult =
    assertNetworkValues(
      program,
      (0 to 8)
        .zip(
          List(
            0.0,
            Double.PositiveInfinity,
            4.82842712474619,
            1.0,
            Double.PositiveInfinity,
            3.82842712474619,
            2.0,
            2.414213562373095,
            3.414213562373095,
          ),
        )
        .toMap,
    )(producedNet)
end GradientWithDistanceFromSourceAndObstacles
