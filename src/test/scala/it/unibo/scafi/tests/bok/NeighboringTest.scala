package it.unibo.scafi.tests.bok

import scala.language.postfixOps
import it.unibo.scafi.FunctionalTestIncarnation.Network
import it.unibo.scafi.ScafiAssertions.assertNetworkValues
import it.unibo.scafi.AbstractScafiProgramTest
import org.scalatest.Assertion

class NeighboringTest extends AbstractScafiProgramTest(List("knowledge/knowledge.md"), "prompts/NeighboringTest.json"):
  override def testCase: String = "count neighbors"
  override def baselineWorkingProgram(): String = "foldhood(0)(_ + _){1}"

  override def programTests(producedNet: Network): Assertion =
    assertNetworkValues((0 to 8).zip(List(4, 6, 4, 6, 9, 6, 4, 6, 4)).toMap)(producedNet)
