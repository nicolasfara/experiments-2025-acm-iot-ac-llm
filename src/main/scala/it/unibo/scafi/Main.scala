package it.unibo.scafi

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.DurationInt

given ExecutionContext = ExecutionContext.global

@main def main(): Unit =
  val tests = Seq(
    CollectMaxIdTest(),
  )

  val res = tests.map: test =>
    Future.sequence(test.executeTest())

  val re = Future.sequence(res).map(_.flatten)
  val assd = Await.result(re, 10.minutes)
  println(assd)