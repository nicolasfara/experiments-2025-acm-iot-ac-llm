/*package it.unibo.scafi.tests.bok.withfindparent

import it.unibo.scafi.ScafiAssertions.assertNetworkValues
import it.unibo.scafi.{ AbstractScafiProgramTest, FunctionalTestIncarnation }
import org.scalatest.Assertion

class CBlockTest
extends AbstractScafiProgramTest(List("knowledge/knowledge-with-findParent.md"), "prompts/CBlock.json"):

override def baselineWorkingProgram(): String =
"""
def C[V](potential: Double, acc: (V, V) => V, local: V, Null: V): V =
  rep(local) { v =>
    acc(local, foldhood(Null)(acc) {
      mux(nbr(findParent(potential)) == mid()) { nbr(v) } { nbr(Null) }
    })
  }""".stripMargin
override def testCase: String = "the C collects values in a node in the network"

override def preAction(): String = """
def findParent(potential: Double): ID = {
  val (minPotential,devIdWithMinPotential) = minHood { nbr{ (potential, mid()) } }
  mux(minPotential < potential) {
    devIdWithMinPotential
  } {
    Int.MaxValue
  }
}
"""

override def postAction(): String =
"""
val potential = classicGradient(mid() == 0)
C(potential, (x: Int, y: Int) => x + y, 1, 0)
""".stripMargin

override def programTests(producedNet: FunctionalTestIncarnation.Network): Assertion =
assertNetworkValues((0 to 8).zip(List(9, 3, 1, 3, 2, 1, 1, 1, 1)).toMap)(producedNet)
end CBlockTest
 */
