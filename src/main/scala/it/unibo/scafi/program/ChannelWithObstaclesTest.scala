package it.unibo.scafi.program

import it.unibo.scafi.test.FunctionalTestIncarnation.Network
import it.unibo.scafi.test.ScafiAssertions.assertNetworkValues
import it.unibo.scafi.test.{ AbstractScafiProgramTest, FunctionalTestIncarnation, ScafiTestResult }

class ChannelWithObstaclesTest
    extends AbstractScafiProgramTest(
      List(
        "knowledge/no-knowledge.md",
        "knowledge/knowledge.md",
        "knowledge/knowledge-with-building-blocks.md"
      ),
      "prompts/ChannelTestWithObstacles.json",
    ):
  override def testCase: String = "create a channel (with obstacles) from the source node to the destination node"
  override def baselineWorkingProgram(): String =
    """
    def G(source: Boolean, value: Double, acc: Double => Double): Double = {
      rep((Double.PositiveInfinity, value)) { case (d, v) =>
        mux(source) {
          (0.0, value)
        } {
          minHoodPlus { (nbr(d) + nbrRange(), acc(nbr(v))) }
        }
      }._2
    }

    def distanceTo(source: Boolean): Double = G(source, 0.0, _ + nbrRange())
    def distanceBetween(source: Boolean, destination: Boolean): Double =
      G(source, distanceTo(destination), x => x)

    branch(sense[Boolean]("obstacle")) {
      false
    } {
      distanceTo(sense[Boolean]("source")) + distanceTo(sense[Boolean]("destination")) <=
       distanceBetween(sense[Boolean]("source"), sense[Boolean]("destination"))
    }
    """.stripMargin

  override def programTests(program: String, producedNet: Network): ScafiTestResult =
    assertNetworkValues(
      program,
      (0 to 8)
        .zip(
          List(
            true, false, false, true, false, false, false, true, true,
          ),
        )
        .toMap,
    )(
      producedNet,
    )
end ChannelWithObstaclesTest
