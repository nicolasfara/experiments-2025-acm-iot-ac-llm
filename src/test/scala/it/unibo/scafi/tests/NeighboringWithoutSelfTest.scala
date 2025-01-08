package it.unibo.scafi.tests

import scala.language.postfixOps

import it.unibo.scafi.FunctionalTestIncarnation.Network
import it.unibo.scafi.ScafiAssertions.assertNetworkValues
import it.unibo.scafi.AbstractScafiProgramTest

class NeighboringWithoutSelfTest extends AbstractScafiProgramTest("prompts/NeighboringWithoutSelfTest.json"):

  override def baselineWorkingProgram(): String = "foldhood(0)(_ + _){if (nbr[Int](mid())==mid()) 0 else 1}"

  override def programTests(producedNet: Network): Unit =
    it should "count neighbors excluding self" in:
      assertNetworkValues((0 to 8).zip(List(3, 5, 3, 5, 8, 5, 3, 5, 3)).toMap)(producedNet)
