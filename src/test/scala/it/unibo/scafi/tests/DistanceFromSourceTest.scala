package it.unibo.scafi.tests

import it.unibo.scafi.AbstractScafiProgramTest
import it.unibo.scafi.FunctionalTestIncarnation.Network
import it.unibo.scafi.ScafiAssertions.assertNetworkValues
import org.scalatest.Assertion

class DistanceFromSourceTest extends AbstractScafiProgramTest(List("knowledge/knowledge.md"), "prompts/DistanceFromSourceTest.json"):
  override def testCase: String = "collect the max ID in the network on each node"
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

  override def programTests(producedNet: Network): Assertion =
    assertNetworkValues(
      (0 to 8)
        .zip(
          List(0.0, 1.0, 2.0, 1.0, 1.4142135623730951, 2.414213562373095, 2.0, 2.414213562373095, 2.8284271247461903),
        )
        .toMap,
    )(producedNet)
end DistanceFromSourceTest
