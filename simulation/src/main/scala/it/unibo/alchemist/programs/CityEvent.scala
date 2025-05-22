package it.unibo.alchemist.programs

import it.unibo.alchemist.boundary.LoadAlchemist
import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._
import org.kaikikm.threadresloader.ResourceLoader

class CityEvent extends AggregateProgram with StandardSensors with ScafiAlchemistSupport with BlockG {
  override def main(): Any = {
    val DENSITY_THRESHOLD_COUNT = 50 // Example: 5 or more other devices in proximity is considered crowded

    // 1. Calculate local device count (number of other devices in proximity)
    // foldhoodPlus sums nbr(1) (1 for each neighbor) from all neighbors, excluding self.
    val localNeighboringDeviceCount = foldhoodPlus(0)(_ + _)(nbr(1))
    // This counts the number of neighbors (other devices) in the local area.
    // 2. Determine if the current node is in a locally overcrowded spot
    val isLocallyOvercrowded = localNeighboringDeviceCount >= DENSITY_THRESHOLD_COUNT
    // 3. Calculate the distance to the nearest overcrowded area using Gradient Cast (G)
    // - source: true if this node is locally overcrowded.
    // - field: 0.0, the initial distance value at a source of overcrowding.
    // - acc: d => d + nbrRange(), accumulates distance. d is distance from neighbor, nbrRange() is cost of hop to that neighbor.
    // - metric: nbrRange, the metric used to determine the "shortest" path (sum of nbrRange() values).
    // The G function will return 0.0 for overcrowded nodes, and for others, the shortest path distance
    // (sum of nbrRange() values) to an overcrowded node. If no overcrowded node is reachable,
    // it might return a value like Double.PositiveInfinity.
    val distanceToNearestCrowdedArea = G[Double](
      source = isLocallyOvercrowded,
      field = 0.0,
      acc = pathDistanceSoFar => pathDistanceSoFar + nbrRange(),
      metric = nbrRange
    )

    // The main function returns this calculated distance.
    // Organizer applications or other parts of the system can then use this value.
    // A low value (e.g., 0.0) indicates being in or very near an overcrowded area.
    // A high value indicates being far from overcrowded areas.
    distanceToNearestCrowdedArea
  }
}

object CityEventApp extends App {
  val simulation = LoadAlchemist.from(ResourceLoader.getResource("yaml/city-event.yml")).getDefault
  simulation.play()
  simulation.run()
  simulation.getError.ifPresent(e => println(s"Error: $e"))
}
