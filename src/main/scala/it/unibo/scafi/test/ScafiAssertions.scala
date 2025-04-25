package it.unibo.scafi.test

import it.unibo.scafi.test.FunctionalTestIncarnation.{ ID, Network }
import it.unibo.scafi.test.ScafiTestResult.{ Success, TestFailed }

object ScafiAssertions:
  /**
   * Asserts the value of all the nodes of the network.
   */
  def assertNetworkValues[T](
      program: String,
      vals: Map[ID, T],
      customEq: Option[(T, T) => Boolean] = None,
  )(net: Network): ScafiTestResult =
    val res = net.ids.forall(id =>
      val actualExport = net.getExport(id)
      val expected = vals.get(id)
      (actualExport, expected) match
        case (Some(e), Some(v)) =>
          if customEq.isDefined then customEq.get(e.root[T](), v)
          else e.root[T]() == v
        case (None, None) => true
        case (None, _) => false
        case _ => false,
    )
    if res then Success(program)
    else TestFailed(program)
  end assertNetworkValues

  def assertNetworkValuesWithPredicate[T](
      program: String,
      pred: (ID, T) => Boolean,
      msg: String = "Assert network values with predicate",
  )(passNotComputed: Boolean = true)(implicit net: Network): ScafiTestResult =
//    withClue(s"""
//         | ${msg}
//         | Actual network: ${net}
//         | Sensor state: ${net.sensorState()}
//         | Neighborhoods: ${net.ids.map(id => id -> net.neighbourhood(id))}
//         | Sample exports
//         | ID=0 => ${net.getExport(0)}
//         | ID=1 => ${net.getExport(1)}
//         |""".stripMargin):
    val res = net.ids.forall(id =>
      val actualExport = net.getExport(id)
      actualExport match
        case Some(v) => pred(id, v.root[T]())
        case None => passNotComputed,
    )
    if res then Success(program)
    else TestFailed(program)
  end assertNetworkValuesWithPredicate
end ScafiAssertions
