package it.unibo.scafi.program

import it.unibo.scafi.test.ScafiAssertions.assertNetworkValues
import it.unibo.scafi.test.{ AbstractScafiProgramTest, FunctionalTestIncarnation, ScafiTestResult }

class GradientWithDistanceFromSource
    extends AbstractScafiProgramTest(
      List(
        "knowledge/no-knowledge.md",
        "knowledge/knowledge.md",
        "knowledge/knowledge-with-building-blocks.md",
      ),
      "prompts/GradientWithDistanceFromSource.json",
    ):
  override def testCase: String = "calculate the gradient with distance from source"
  override def baselineWorkingProgram(): String =
    """
    rep(Double.PositiveInfinity) { case d =>
      mux(sense[Boolean]("source")) {
        0.0
      } {
        minHoodPlus(nbr(d) + nbrRange())
      }
    }
    """.stripMargin

  override def programTests(program: String, producedNet: FunctionalTestIncarnation.Network): ScafiTestResult =
    assertNetworkValues(
      program,
      (0 to 8)
        .zip(
          List(0.0, 1.0, 2.0, 1.0, 1.4142135623730951, 2.414213562373095, 2.0, 2.414213562373095, 2.8284271247461903),
        )
        .toMap,
    )(producedNet)
end GradientWithDistanceFromSource
