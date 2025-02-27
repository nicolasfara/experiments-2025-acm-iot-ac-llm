package it.unibo.alchemist.programs

import it.unibo.alchemist.boundary.LoadAlchemist
import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._
import org.kaikikm.threadresloader.ResourceLoader

class ScrProgram extends AggregateProgram with StandardSensors with ScafiAlchemistSupport with BlockG with BlockC with BlockS {
  private lazy val temp = randomGenerator().nextDouble() * 100
  override def main(): Any = {
    val isLeader = S(100, nbrRange)
    node.put("isLeader", isLeader)
    val temperature = temp
    val potential = G[Double](isLeader, 0.0, _ + nbrRange(), nbrRange)
    val leaderId = G[Double](isLeader, mid() / 2, identity, nbrRange)
    node.put("leaderId", leaderId)
    val areaTemperature = C[Double, Double](potential, _ + _, temperature, 0.0)
    val areaSize = C[Double, Int](potential, _ + _, 1, 0)
    val averageTemperature = if (areaSize > 0) areaTemperature / areaSize else 0.0
    val alarm = averageTemperature > 30.0
    G(isLeader, alarm, (a:Boolean) => a, nbrRange)
  }
}

object ScrProgramApp extends App {
  val simulation = LoadAlchemist.from(ResourceLoader.getResource("yaml/scr.yml")).getDefault
  simulation.play()
  simulation.run()
  simulation.getError.ifPresent(e => println(s"Error: $e"))
}
