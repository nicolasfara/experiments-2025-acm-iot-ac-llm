package it.unibo.alchemist.programs

import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._

class GradientProgram extends AggregateProgram with StandardSensors with ScafiAlchemistSupport with BlockG {
  override def main(): Any = {
    val source = mid() == 0
    node.put("source", source)
    rep(Double.PositiveInfinity) { distance =>
      mux(source) {
        0.0
      } {
        minHood(nbr(distance) + nbrRange())
      }
    }
  }
}