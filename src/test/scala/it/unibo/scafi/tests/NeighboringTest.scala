package it.unibo.scafi.tests

import scala.language.postfixOps

import it.unibo.scafi.FunctionalTestIncarnation.Network
import it.unibo.scafi.ScafiAssertions.assertNetworkValues
import it.unibo.scafi.AbstractScafiProgramTest

class NeighboringTest extends AbstractScafiProgramTest("prompts/NeighboringTest.json"):

  override def baselineWorkingProgram(): String = "foldhood(0)(_ + _){1}"

  override def programTests(producedNet: Network): Unit =
    it should "count neighbors" in:
      assertNetworkValues((0 to 8).zip(List(4, 6, 4, 6, 9, 6, 4, 6, 4)).toMap)
