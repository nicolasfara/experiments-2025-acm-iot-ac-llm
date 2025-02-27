package it.unibo.alchemist.programs

import it.unibo.alchemist.boundary.LoadAlchemist
import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._
import org.kaikikm.threadresloader.ResourceLoader

class GradientWithObstaclesProgram extends AggregateProgram with StandardSensors with ScafiAlchemistSupport with BlockG {
  override def main(): Any = {
    val source = mid() == 0
    val isObstacle = sense[Boolean]("isObstacle")
    node.put("source", source)
    val distances = rep(Double.PositiveInfinity) { d =>
      mux(source) {
        0.0
      } {
        mux(isObstacle) {
          Double.PositiveInfinity
        } {
          minHoodPlus(nbr(d) + nbrRange())
        }
      }
    }
    distances
  }
}

object GradientWithObstaclesProgramApp extends App {
  val simulation = LoadAlchemist.from(ResourceLoader.getResource("yaml/gradient-with-obstacles.yml")).getDefault
  simulation.play()
  simulation.run()
  simulation.getError.ifPresent(e => println(s"Error: $e"))
}