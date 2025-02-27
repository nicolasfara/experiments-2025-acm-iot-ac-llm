package it.unibo.alchemist.programs

import it.unibo.alchemist.boundary.LoadAlchemist
import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._
import org.kaikikm.threadresloader.ResourceLoader

object Gradient extends App {
  val simulation = LoadAlchemist.from(ResourceLoader.getResource("yaml/gradient.yml")).getDefault
  simulation.play()
  simulation.run()
  simulation.getError.ifPresent(e => println(s"Error: $e"))
}