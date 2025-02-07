
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

given node: (BasicAggregateInterpreter & StandardSensors & BlockG & BlockC) =
  new BasicAggregateInterpreter with StandardSensors with BlockG with BlockC

runProgram {
  import node.*
  

    val obstacle = sense[Boolean]("obstacle") // Assuming true means obstacle
  val destination = sense[Boolean]("destination")
  val source = sense[Boolean]("source")

  val adjustedNbrRange = () => if (obstacle) Double.PositiveInfinity else nbrRange()

  val potential = G[Double](
    source = destination,
    field = 0.0,
    acc = _ + nbrRange(),
    metric = adjustedNbrRange
  )

  C[Double, Boolean](
    potential = potential,
    acc = _ || _,
    local = source,
    Null = false
  )


  
}(net)(using node)._1

