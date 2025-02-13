package it.unibo.scafi.program

import it.unibo.scafi.test.ScafiAssertions.assertNetworkValues
import it.unibo.scafi.test.{AbstractScafiProgramTest, ScafiTestResult}
import it.unibo.scafi.test.FunctionalTestIncarnation.Network

class NeighboringTest
    extends AbstractScafiProgramTest(
      List("knowledge-with-building-blocks.md"),
      "prompts/NeighboringTest.json",
    ):
  override def testCase: String = "count neighbors"
  override def baselineWorkingProgram(): String = "foldhood(0)(_ + _){1}"

  override def programTests(program: String, producedNet: Network): ScafiTestResult =
    assertNetworkValues(program, (0 to 8).zip(List(4, 6, 4, 6, 9, 6, 4, 6, 4)).toMap)(producedNet)
