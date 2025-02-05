/*
package it.unibo.scafi.tests.bok.withgradient


import it.unibo.scafi.FunctionalTestIncarnation.Network
import it.unibo.scafi.ScafiAssertions.assertNetworkValues
import it.unibo.scafi.{ AbstractScafiProgramTest, FunctionalTestIncarnation }
import org.scalatest.Assertion

class FindParentTest extends AbstractScafiProgramTest(List("knowledge/knowledge-with-G.md"), "prompts/FindParentTest.json", raw = true):
override def testCase: String = "the findParent function must return the ID of the parent along the potential"
override def baselineWorkingProgram(): String =
  """
  def findParent(potential: Double): ID = {
    val (minPotential,devIdWithMinPotential) = minHood { nbr{ (potential, mid()) } }
    mux(minPotential < potential) {
      devIdWithMinPotential
    } {
      Int.MaxValue
    }
  }""".stripMargin

override def postAction(): String =
  """
    |val potential = classicGradient(mid() == 0)
    |findParent(potential)""".stripMargin

override def programTests(producedNet: Network): Assertion =
  assertNetworkValues((0 to 8).zip(List(2147483647, 0, 1, 0, 0, 1, 3, 3, 4)).toMap)(producedNet)
 */
