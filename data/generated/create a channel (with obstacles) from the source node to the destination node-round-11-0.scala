
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
  

  val isSource = S(2)
val areaTemperature = C[Double, Double](
  potential = G[Double](isSource, 0, _ + nbrRange(), nbrRange),
  acc = _ + _,
  local = sense[Double]("temperature"),
  Null = 0.0
)

val areaSize = C[Double, Int](
  potential = G[Double](isSource, 0, _ + nbrRange(), nbrRange),
  acc = _ + _,
  local = 1,
  Null = 0
)

val averageTemperature = if (areaSize > 0) areaTemperature / areaSize else 0.0

G[Boolean](
  source = isSource && averageTemperature > 30,
  field = averageTemperature > 30,
  acc = (a, _) => a,
  metric = nbrRange
)


  
}(net)(using node)._1

