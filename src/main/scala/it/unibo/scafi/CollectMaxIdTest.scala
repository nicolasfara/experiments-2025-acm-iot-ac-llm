package it.unibo.scafi

import it.unibo.scafi.test.{ AbstractScafiProgramTest, FunctionalTestIncarnation, ScafiTestResult }
import it.unibo.scafi.test.FunctionalTestIncarnation.Network

class CollectMaxIdTest
    extends AbstractScafiProgramTest(
      List("knowledge/knowledge-with-building-blocks.md"),
      "prompts/CollectMaxIdTest.json",
    ):
  override def testCase: String = "collect the max ID in the network on each node"
  override def baselineWorkingProgram(): String =
    """
    rep(mid()) { prevMaxId =>
      prevMaxId max foldhood(mid())(_ max _){ nbr(prevMaxId) }
    }
    """.stripMargin

  override def programTests(producedNet: Network): ScafiTestResult = ???
//  override def programTests(producedNet: Network): Assertion =
//    assertNetworkValues((0 to 8).zip(List(8, 8, 8, 8, 8, 8, 8, 8, 8)).toMap)(producedNet)
