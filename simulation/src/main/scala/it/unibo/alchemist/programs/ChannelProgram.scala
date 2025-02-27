package it.unibo.alchemist.programs

import it.unibo.alchemist.boundary.LoadAlchemist
import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._
import org.kaikikm.threadresloader.ResourceLoader

class ChannelProgram extends AggregateProgram with StandardSensors with ScafiAlchemistSupport with BlockG with BlockC {
  override def main(): Boolean = {
    val obstacle = mid() % 9 == 0 && mid() != 0
    val sourceCondition = mid() == 0
    val destinationCondition = mid() == 399
    node.put("source", sourceCondition)
    node.put("destination", destinationCondition)
    node.put("obstacle", obstacle)

    val potential = G[Double](
      source = sourceCondition,
      field = 0.0,
      acc = _ + nbrRange(),
      metric = () => nbrRange()
    )

    C[Double, Boolean](
      potential = potential,
      acc = _ || _,
      local = destinationCondition,
      Null = false
    )
  }
}

object ChannelProgramApp extends App {
  val simulation = LoadAlchemist.from(ResourceLoader.getResource("yaml/channel.yml")).getDefault
  simulation.play()
  simulation.run()
  simulation.getError.ifPresent(e => println(s"Error: $e"))
}
