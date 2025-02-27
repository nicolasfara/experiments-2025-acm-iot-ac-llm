package it.unibo.alchemist.programs

import it.unibo.alchemist.boundary.LoadAlchemist
import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._
import org.kaikikm.threadresloader.ResourceLoader

class ChannelWithObstaclesProgram extends AggregateProgram with StandardSensors with ScafiAlchemistSupport with BlockG with BlockC {

  override def main(): Boolean = {
    val obstacleDetected = sense[Boolean]("isObstacle")
    val destinationReached = mid() == 399
    val sourceNode = mid() == 0
    node.put("source", sourceNode)
    node.put("destination", destinationReached)
    //node.put("isObstacle", obstacleDetected)

    def obstacleAwareMetric(): Double = {
      if (obstacleDetected) Double.PositiveInfinity else nbrRange()
    }

    val potential = G[Double](
      source = destinationReached,
      field = 0.0,
      acc = _ + nbrRange(),
      metric = obstacleAwareMetric
    )

    C[Double, Boolean](
      potential = potential,
      acc = _ || _,
      local = sourceNode,
      Null = false
    )
  }
}

object ChannelWithObstacleProgramApp extends App {
  val simulation = LoadAlchemist.from(ResourceLoader.getResource("yaml/channel-with-obstacles.yml")).getDefault
  simulation.play()
  simulation.run()
  simulation.getError.ifPresent(e => println(s"Error: $e"))
}
