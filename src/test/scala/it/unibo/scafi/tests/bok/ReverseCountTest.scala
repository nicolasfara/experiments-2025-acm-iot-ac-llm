package it.unibo.scafi.tests.bok

import it.unibo.scafi.AbstractScafiProgramTest
import it.unibo.scafi.FunctionalTestIncarnation.Network
import it.unibo.scafi.ScafiAssertions.assertNetworkValues
import org.scalatest.Assertion

class ReverseCountTest
    extends AbstractScafiProgramTest(
      List("knowledge/knowledge-with-building-blocks.md"),
      "prompts/ReverseCountTest.json",
    ):
  override def testCase: String = "collect the max ID in the network on each node"
  override def baselineWorkingProgram(): String =
    """
    rep(1000) { prevCount =>
      if (prevCount > 0) prevCount - 1 else 0
    }
    """.stripMargin

  override def programTests(producedNet: Network): Assertion =
    assertNetworkValues((0 to 8).zip(List(931, 958, 941, 952, 950, 948, 942, 940, 938)).toMap)(producedNet)
