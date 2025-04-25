package it.unibo.scafi.program

import it.unibo.scafi.test.ScafiAssertions.assertNetworkValues
import it.unibo.scafi.test.{ AbstractScafiProgramTest, ScafiTestResult }
import it.unibo.scafi.test.FunctionalTestIncarnation.Network

class NeighboringWithoutSelfTest
    extends AbstractScafiProgramTest(
      List(
        "knowledge/no-knowledge.md",
        "knowledge/knowledge.md",
        "knowledge/knowledge-with-building-blocks.md",
      ),
      "prompts/NeighboringWithoutSelfTest.json",
    ):
  override def testCase: String = "count neighbors excluding self"
  override def baselineWorkingProgram(): String = "foldhood(0)(_ + _){if (nbr[Int](mid())==mid()) 0 else 1}"

  override def programTests(program: String, producedNet: Network): ScafiTestResult =
    assertNetworkValues(program, (0 to 8).zip(List(3, 5, 3, 5, 8, 5, 3, 5, 3)).toMap)(producedNet)
