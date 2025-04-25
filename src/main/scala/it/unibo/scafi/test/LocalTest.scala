//package it.unibo.scafi.test

//import it.unibo.scafi.test.FunctionalTestIncarnation.*
//import it.unibo.scafi.test.ScafiTestUtils.runProgram
//import it.unibo.scafi.config.GridSettings
//
//import scala.collection.mutable
//
//val net: Network & SimulatorOps =
//  simulatorFactory.gridLike(
//    GridSettings(3, 3, 1, 1),
//    rng = 1.5,
//    seeds = Seeds(187372311, 187372311, 187372311),
//    lsnsMap = mutable.Map(
//      "temperature" -> mutable.Map(
//        0 -> 10.0,
//        1 -> 20.0,
//        2 -> 30.0,
//        3 -> 40.0,
//        4 -> 50.0,
//        5 -> 60.0,
//        6 -> 70.0,
//        7 -> 80.0,
//        8 -> 90.0,
//      ),
//      "source" -> mutable.Map(
//        0 -> true,
//        1 -> false,
//        2 -> false,
//        3 -> false,
//        4 -> false,
//        5 -> false,
//        6 -> false,
//        7 -> false,
//        8 -> false,
//      ),
//      "destination" -> mutable.Map(
//        0 -> false,
//        1 -> false,
//        2 -> false,
//        3 -> false,
//        4 -> false,
//        5 -> false,
//        6 -> false,
//        7 -> false,
//        8 -> true,
//      ),
//      "obstacle" -> mutable.Map(
//        0 -> false,
//        1 -> true,
//        2 -> false,
//        3 -> false,
//        4 -> true,
//        5 -> false,
//        6 -> false,
//        7 -> false,
//        8 -> false,
//      ),
//    ),
//  )
//
//given node: (BasicAggregateInterpreter & StandardSensors & BlockG & BlockC & BlockS) =
//  new BasicAggregateInterpreter with StandardSensors with BlockG with BlockC with BlockS
//
//def test() = runProgram {
//  import node.*
//  rep(Double.PositiveInfinity) { case d =>
//    branch(sense[Boolean]("obstacle")) {
//      Double.PositiveInfinity
//    } {
//      mux(sense[Boolean]("source")) {
//        0.0
//      } {
//        minHoodPlus(nbr(d) + nbrRange())
//      }
//    }
//  }
//}(net)(using node)._1
