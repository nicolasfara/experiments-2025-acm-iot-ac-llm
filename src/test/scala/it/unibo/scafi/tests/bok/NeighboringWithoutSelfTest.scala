package it.unibo.scafi.tests.bok

import scala.language.postfixOps
import it.unibo.scafi.FunctionalTestIncarnation.Network
import it.unibo.scafi.ScafiAssertions.assertNetworkValues
import it.unibo.scafi.AbstractScafiProgramTest
import org.scalatest.Assertion

class NeighboringWithoutSelfTest
    extends AbstractScafiProgramTest(
      List("knowledge/knowledge-with-building-blocks.md"),
      "prompts/NeighboringWithoutSelfTest.json",
    ):
  override def testCase: String = "count neighbors excluding self"
  override def baselineWorkingProgram(): String = "foldhood(0)(_ + _){if (nbr[Int](mid())==mid()) 0 else 1}"

  override def programTests(producedNet: Network): Assertion =
    assertNetworkValues((0 to 8).zip(List(3, 5, 3, 5, 8, 5, 3, 5, 3)).toMap)(producedNet)
