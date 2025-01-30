package it.unibo.scafi.tests.bok

import it.unibo.scafi.AbstractScafiProgramTest
import it.unibo.scafi.FunctionalTestIncarnation.{ID, Network}
import it.unibo.scafi.ScafiAssertions.assertNetworkValuesWithPredicate
import org.scalatest.Assertion

import scala.language.postfixOps

class NeighboringIdsTest extends AbstractScafiProgramTest(List("knowledge/knowledge.md"), "prompts/NeighboringIdsTest.json"):
  override def testCase: String = "gather the IDs of their neighbors"
  override def baselineWorkingProgram(): String = "foldhood(List[Int]())(_++_){List(nbr[Int](mid()))}"

  override def programTests(producedNet: Network): Assertion =
    val values = (0 to 8)
      .zip(
        List(
          Set(0, 1, 3, 4),
          Set(0, 1, 2, 4, 3, 5),
          Set(1, 2, 4, 5),
          Set(0, 1, 3, 4, 6, 7),
          Set(0, 1, 2, 3, 4, 5, 6, 7, 8),
          Set(1, 2, 4, 5, 7, 8),
          Set(3, 4, 6, 7),
          Set(3, 4, 5, 6, 7, 8),
          Set(4, 5, 7, 8),
        ),
      )
      .toMap
    assertNetworkValuesWithPredicate((id: ID, v: List[ID]) => v.sorted == values(id).toList.sorted)(true)(producedNet)
end NeighboringIdsTest
