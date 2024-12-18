/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi

import it.unibo.scafi.incarnations.AbstractTestIncarnation
import it.unibo.scafi.incarnations.BasicSimulationIncarnation.Builtins
import it.unibo.scafi.lib.StandardLibrary
import it.unibo.scafi.simulation.Simulation

object FunctionalTestIncarnation extends AbstractTestIncarnation, StandardLibrary, Simulation:
  override implicit val idBounded: Builtins.Bounded[ID] = Builtins.Bounded.of_i
//}
