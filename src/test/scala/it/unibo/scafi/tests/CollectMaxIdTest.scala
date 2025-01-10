package it.unibo.scafi.tests

import it.unibo.scafi.AbstractScafiProgramTest
import it.unibo.scafi.FunctionalTestIncarnation.Network
import it.unibo.scafi.ScafiAssertions.assertNetworkValues
import org.scalatest.Assertion

class CollectMaxIdTest extends AbstractScafiProgramTest("prompts/CollectMaxIdTest.json"):
  override def testCase: String = "collect the max ID in the network on each node"
  override def baselineWorkingProgram(): String =
    """
    rep(mid()) { prevMaxId =>
      prevMaxId max foldhood(mid())(_ max _){ nbr(prevMaxId) }
    }
    """.stripMargin

  override def programTests(producedNet: Network): Assertion =
    assertNetworkValues((0 to 8).zip(List(8, 8, 8, 8, 8, 8, 8, 8, 8)).toMap)(producedNet)
