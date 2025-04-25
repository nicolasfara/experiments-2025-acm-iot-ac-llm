package it.unibo.scafi.program

import it.unibo.scafi.test.FunctionalTestIncarnation.Network
import it.unibo.scafi.test.ScafiAssertions.assertNetworkValues
import it.unibo.scafi.test.{ AbstractScafiProgramTest, FunctionalTestIncarnation, ScafiTestResult }

class SCRTest
    extends AbstractScafiProgramTest(
      List(
        "knowledge/no-knowledge.md",
        "knowledge/knowledge.md",
        "knowledge/knowledge-with-building-blocks.md",
      ),
      "prompts/SCRTest.json",
    ):
  override def testCase: String =
    "SCR where temperature is above 30 degrees within the area"
  override def baselineWorkingProgram(): String =
    """
    val isLeader = S(2, nbrRange)
    val potential = G[Double](isLeader, 0, _ + nbrRange(), nbrRange)
    val areaTemperature = C[Double, Double](potential, _ + _, sense[Double]("temperature"), 0)
    val areaSize = C[Double, Int](potential, _ + _, 1, 0)
    val avgTemperature = areaTemperature / areaSize
    G[Boolean](isLeader, avgTemperature > 30, a => a, nbrRange)


    """.stripMargin

  override def programTests(program: String, producedNet: Network): ScafiTestResult =
    assertNetworkValues(
      program,
      (0 to 8)
        .zip(
          List(false, false, false, true, true, true, true, true, true),
        )
        .toMap,
    )(
      producedNet,
    )
end SCRTest
