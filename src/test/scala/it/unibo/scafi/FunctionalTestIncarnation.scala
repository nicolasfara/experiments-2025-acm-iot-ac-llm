/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
 */

package it.unibo.scafi

import it.unibo.scafi.incarnations.AbstractTestIncarnation
import it.unibo.scafi.lib.StandardLibrary
import it.unibo.scafi.simulation.Simulation

object FunctionalTestIncarnation extends AbstractTestIncarnation with Simulation with StandardLibrary:
  import Builtins.Bounded
  implicit override val idBounded: Bounded[ID] = Builtins.Bounded.of_i

  class Test extends AggregateProgram with StandardSensors with BlockC with BlockG with BlockS:
    override def main(): Any =
      val isLeader = S(2, nbrRange)
      val potential = G[Double](isLeader, 0, _ + nbrRange(), nbrRange)
      val areaTemperature = C[Double, Double](potential, _ + _, sense[Double]("temperature"), 0)
      val areaSize = C[Double, Int](potential, _ + _, 1, 0)
      val avgTemperature = areaTemperature / areaSize
      G[Boolean](isLeader, avgTemperature < 30, a => a, nbrRange)
