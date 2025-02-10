
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
  val isLeader = S(5, nbrRange) // Elect leaders with influence radius 5
  val potential = G[Double](isLeader, 0.0, _ + nbrRange(), nbrRange) // Gradient from leaders

  val areaTemperature = C[Double, Double](potential, _ + _, sense[Double]("temperature"), 0.0) // Collect area temperature

  val areaSize = C[Double, Int](potential, _ + _, 1, 0) // Collect number of nodes in the area

  val averageAreaTemperature = if (areaSize > 0) areaTemperature / areaSize else 0.0 // Compute average temperature

  val alarm = averageAreaTemperature > 30.0 // Check if alarm should be raised

  G[Boolean](isLeader, alarm, (a: Boolean) => a, nbrRange) // Broadcast alarm within the area
}


  
}(net)(using node)._1

