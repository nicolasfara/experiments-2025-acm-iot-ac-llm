package it.unibo.scafi.program.llm

import scala.collection.mutable
import scala.io.{ BufferedSource, Source }

import cats.effect.{ IO, Resource }
import it.unibo.scafi.program.utils.PromptUtils.generatePreamblePrompt
import it.unibo.scafi.program.utils.StringUtils

class FileBasedReplayer(val model: String) extends CodeGeneratorService:
  private val candidateKnowledgeFileNames = List(
    "knowledge/no-knowledge.md",
    "knowledge/knowledge.md",
    "knowledge/knowledge-with-building-blocks.md",
  )
  private val prompts = Map(
    "create a channel from the source node called 'source' to the destination node called 'destination'" -> "ChannelTest",
    "create a channel from the source node called 'source' to the destination node called 'destination' and it should avoid any obstacles in the path (namely, boolean sensors called 'obstacle'). It should return true if the channel was successfully created, and false otherwise." -> "ChannelTestWithObstacles",
    "compute the max ID in the whole network" -> "CollectMaxIdTest",
    "compute the euclidean distance from the source using a sensor named 'source'" -> "GradientWithDistanceFromSource",
    "compute the euclidean distance from the source using a sensor named 'source' and it should avoid any obstacles in the path (namely, boolean sensors called 'obstacle')." -> "GradientWithDistanceFromSourceWithObstacles",
    "gather a list IDs of their neighbors" -> "NeighboringIdsTest",
    "calculate the min distance ONLY from neighbors" -> "NeighboringMinDistanceTest",
    "count neighbors" -> "NeighboringTest",
    "count neighbors excluding self" -> "NeighboringWithoutSelfTest",
    "evolve backwards a value from 1000 to 0" -> "ReverseCountTest",
    "Compute several areas where it is computed the area-wise temperature and send back (in broadcast) within the area an alarm to the area when the temperature is above 30 degrees" -> "SCRTest",
  )
  private val currentIterations = mutable.Map[(String, String), Int]().withDefault((_, _) => 1)

  private def getSource(fileName: String): IO[BufferedSource] = IO(Source.fromResource(fileName))

  private def readFile(src: Source): IO[String] = IO(src.getLines.mkString)

  private def readFileBreakLines(src: Source): IO[String] = IO(src.getLines.mkString("\n"))

  private def closeSource(src: Source): IO[Unit] = IO(src.close)

  private def openKnowledgeFile(file: String): Resource[IO, BufferedSource] =
    Resource.make(getSource(file))(f => closeSource(f))

  private def compareContent(fileName: String, content: String): IO[Boolean] =
    openKnowledgeFile(fileName).use(readFile).map { fileContent => content == fileContent }

  private def getFileBasedOnKnowledge(knowledgeContent: String): IO[Option[String]] =
    candidateKnowledgeFileNames.map { file =>
      compareContent(file, knowledgeContent).map { isEqual =>
        if isEqual then Some(file.replace("knowledge/", "").replace(".md", "")) else None
      }
    }.sequence.map(_.flatten.headOption)

  private def getTestBasedOnPrompt(prompt: String): IO[Option[String]] = IO:
    prompts.get(prompt)

  private def readLlmResponse(testFile: String, knowledgeDirectory: String, iteration: Int): IO[String] =
    openKnowledgeFile(s"$model/$knowledgeDirectory/$testFile/$iteration.txt").use(readFileBreakLines)

  override def generateRaw(localKnowledge: String, preamble: String, prompt: String): IO[String] =
    for
      knowledgeFileName <- getFileBasedOnKnowledge(localKnowledge)
      testFileName <- getTestBasedOnPrompt(prompt)
      counter = currentIterations((knowledgeFileName.get, testFileName.get))
      _ = currentIterations((knowledgeFileName.get, testFileName.get)) = counter + 1
      response <- readLlmResponse(testFileName.get, knowledgeFileName.get, counter)
    yield StringUtils.refineOutput(response)

  override def generateMain(localKnowledge: String, prompt: String): IO[String] =
    generateRaw(localKnowledge, generatePreamblePrompt(), prompt)

  override def toString: String = model
end FileBasedReplayer
