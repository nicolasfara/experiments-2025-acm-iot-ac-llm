package it.unibo.alchemist

import it.unibo.alchemist.boundary.LoadAlchemist
import org.kaikikm.threadresloader.ResourceLoader

object AlchemistMain extends App {
  val simulation = LoadAlchemist.from(ResourceLoader.getResource("yaml/gradient.yml")).getDefault
  simulation.play()
  simulation.run()
  simulation.getError.ifPresent(e => println(s"Error: $e"))
}