package it.unibo.scafi

import it.unibo.scafi.platform.SimulationPlatform
import it.unibo.scafi.core.Core
import it.unibo.scafi.incarnations.BasicSimulationIncarnation.{ AggregateInterpreter, SimulatorOps }

object ScafiTestUtils:

  def runProgram(exp: => Any, ntimes: Int = 500)(net: SimulationPlatform#Network & SimulatorOps)(using
      node: AggregateInterpreter,
  ): (SimulationPlatform#Network, Seq[Core#ID]) =
    var endNetwork: SimulationPlatform#Network = null
    val executionSequence =
      net.execMany(
        node = node,
        exp = exp,
        size = ntimes,
        action = (_, i) => if i % ntimes == 0 then endNetwork = net else endNetwork = endNetwork,
      )
    (endNetwork, executionSequence)
