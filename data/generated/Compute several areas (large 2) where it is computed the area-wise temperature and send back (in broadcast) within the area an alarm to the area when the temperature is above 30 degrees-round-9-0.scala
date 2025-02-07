
import it.unibo.scafi.FunctionalTestIncarnation.*
import it.unibo.scafi.ScafiTestUtils.runProgram
import it.unibo.scafi.config.GridSettings
import scala.collection.mutable.Map


val net: Network & SimulatorOps =
  simulatorFactory.gridLike(
    GridSettings(3, 3, 1, 1),
    rng = 1.5,
    seeds = Seeds(187372311, 187372311, 187372311),
    lsnsMap = Map(
      "temperature" -> Map(
        0 -> 10.0,
        1 -> 20.0,
        2 -> 30.0,
        3 -> 40.0,
        4 -> 50.0,
        5 -> 60.0,
        6 -> 70.0,
        7 -> 80.0,
        8 -> 90.0,
      ),
      "source" -> Map(
        0 -> true,
        1 -> false,
        2 -> false,
        3 -> false,
        4 -> false,
        5 -> false,
        6 -> false,
        7 -> false,
        8 -> false,
      ),
      "destination" -> Map(
        0 -> false,
        1 -> false,
        2 -> false,
        3 -> false,
        4 -> false,
        5 -> false,
        6 -> false,
        7 -> false,
        8 -> true,
      ),
      "obstacle" -> Map(
        0 -> false,
        1 -> true,
        2 -> false,
        3 -> false,
        4 -> true,
        5 -> false,
        6 -> false,
        7 -> false,
        8 -> false,
      ),
    ),
  )

given node: (BasicAggregateInterpreter & StandardSensors & BlockG & BlockC & BlockS) =
  new BasicAggregateInterpreter with StandardSensors with BlockG with BlockC with BlockS

runProgram {
  import node.*
  

  {
  val areaId = rep(0)(_ + 1) % 3 // Example: 3 areas
  val temperature = sense[Double]("temperature")
  val alarm = rep(false) { _ =>
    val areaTemperatureSum = foldhood(0.0)(_ + _)(mux(nbr(areaId == areaId))(nbr(temperature))(0.0))
    val areaNodeCount = foldhood(0)(_ + _)(mux(nbr(areaId == areaId))(nbr(1))(0))
    val areaAverageTemperature = if (areaNodeCount > 0) areaTemperatureSum / areaNodeCount else 0.0
    areaAverageTemperature > 30.0
  }
  alarm
}


  
}(net)(using node)._1

