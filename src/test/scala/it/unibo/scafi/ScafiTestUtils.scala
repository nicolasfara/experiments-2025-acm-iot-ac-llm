package it.unibo.scafi

import it.unibo.scafi.FunctionalTestIncarnation.*

object ScafiTestUtils:

  def runProgram(exp: => Any, ntimes: Int = 500)(net: Network & SimulatorOps)(using
      node: AggregateInterpreter,
  ): (Network, Seq[ID]) =
    var endNetwork: Network = null
    val executionSequence =
      net.execMany(
        node = node,
        exp = exp,
        size = ntimes,
        action = (_, i) => if i % ntimes == 0 then endNetwork = net else endNetwork = endNetwork,
      )
    (endNetwork, executionSequence)

  def executeFromString[Result](program: String, preamble: String = ""): Result =
    dotty.tools.repl
      .ScriptEngine()
      .eval(
        s"""
         |import it.unibo.scafi.FunctionalTestIncarnation.*
         |import it.unibo.scafi.ScafiTestUtils.runProgram
         |import it.unibo.scafi.config.GridSettings
         |$preamble
         |
         |val net: Network & SimulatorOps =
         |  simulatorFactory.gridLike(GridSettings(3, 3, 1, 1), rng = 1.5, seeds = Seeds(187372311, 187372311, 187372311))
         |
         |given node: (BasicAggregateInterpreter & StandardSensorNames) = new BasicAggregateInterpreter with StandardSensorNames
         |
         |runProgram {
         |  import node.*
         |  $program
         |}(net)(using node)._1
         |
         |""".stripMargin,
      )
      .asInstanceOf[Result]
end ScafiTestUtils
