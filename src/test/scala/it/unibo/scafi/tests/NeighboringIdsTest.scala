package it.unibo.scafi.tests

import scala.language.postfixOps

import it.unibo.scafi.FunctionalTestIncarnation.{ ID, Network }
import it.unibo.scafi.ScafiAssertions.assertNetworkValuesWithPredicate
import it.unibo.scafi.{ AbstractScafiProgramTest, CodeGeneratorService }

private val neighboringIdsTest = new CodeGeneratorService:
  override def localKnowledge: String = ""
  override def generateCode(prompt: String): String =
    """
      foldhood(List[Int]())(_++_){List(nbr[Int](mid()))}
    """

private val neighborCountPrompts = List(
  "",
)

class NeighboringIdsTest extends AbstractScafiProgramTest(neighborCountPrompts, neighboringIdsTest):

  override def behaviors(producedNet: Network): Unit =
    it should "gather the IDs of their neighbors" in:
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
