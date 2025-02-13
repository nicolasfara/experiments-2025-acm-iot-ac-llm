package it.unibo.scafi.program

import it.unibo.scafi.test.ScafiAssertions.assertNetworkValues
import it.unibo.scafi.test.{AbstractScafiProgramTest, ScafiTestResult}
import it.unibo.scafi.test.FunctionalTestIncarnation.Network

class ReverseCountTest
    extends AbstractScafiProgramTest(
      List("knowledge/knowledge-with-building-blocks.md"),
      "prompts/ReverseCountTest.json",
    ):
  override def testCase: String = "count down from 1000 to 0"
  override def baselineWorkingProgram(): String =
    """
    rep(1000) { prevCount =>
      if (prevCount > 0) prevCount - 1 else 0
    }
    """.stripMargin

  override def programTests(program: String, producedNet: Network): ScafiTestResult =
    assertNetworkValues(program, (0 to 8).zip(List(931, 958, 941, 952, 950, 948, 942, 940, 938)).toMap)(producedNet)
