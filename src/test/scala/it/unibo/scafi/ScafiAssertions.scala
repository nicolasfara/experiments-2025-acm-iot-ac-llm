package it.unibo.scafi

import it.unibo.scafi.FunctionalTestIncarnation.{ ID, Network }
import org.scalatest.Assertion
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.shouldBe

object ScafiAssertions extends Matchers:

  /**
   * Asserts the value of all the nodes of the network.
   */
  def assertNetworkValues[T](
      vals: Map[ID, T],
      customEq: Option[(T, T) => Boolean] = None,
      msg: String = "Assert Network Values",
  )(net: Network): Assertion = withClue(s"""
         | ${msg}
         | Actual network: ${net}
         | Sensor state: ${net.sensorState()}
         | Neighborhoods: ${net.ids.map(id => id -> net.neighbourhood(id))}
         | Sample exports
         | ID=0 => ${net.getExport(0)}
         | ID=1 => ${net.getExport(1)}
         |
         | Expected values: ${vals}
              """.stripMargin):
    net.ids.forall(id =>
      val actualExport = net.getExport(id)
      val expected = vals.get(id)
      (actualExport, expected) match
        case (Some(e), Some(v)) =>
          if customEq.isDefined then customEq.get(e.root[T](), v)
          else e.root[T]() == v
        case (None, None) => true
        case (None, _) => false
        case _ => false,
    ) shouldBe true

  def assertNetworkValuesWithPredicate[T](
      pred: (ID, T) => Boolean,
      msg: String = "Assert network values with predicate",
  )(passNotComputed: Boolean = true)(implicit net: Network): Assertion =
    withClue(s"""
         | ${msg}
         | Actual network: ${net}
         | Sensor state: ${net.sensorState()}
         | Neighborhoods: ${net.ids.map(id => id -> net.neighbourhood(id))}
         | Sample exports
         | ID=0 => ${net.getExport(0)}
         | ID=1 => ${net.getExport(1)}
         |""".stripMargin):
      net.ids.forall(id =>
        val actualExport = net.getExport(id)
        actualExport match
          case Some(v) => pred(id, v.root[T]())
          case None => passNotComputed,
      ) shouldBe true
end ScafiAssertions
