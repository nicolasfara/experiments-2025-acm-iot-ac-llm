package it.unibo.scafi

import io.circe.generic.auto.*
import io.circe.parser.*
import it.unibo.scafi.FunctionalTestIncarnation.Network
import it.unibo.scafi.ScafiTestUtils.*
import org.scalatest.Assertion
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers

import java.nio.file.{ Files, Path }
import scala.concurrent.Future
import scala.io.Source
import scala.jdk.CollectionConverters.*
import scala.util.{ Failure, Success, Try, Using }

final case class ScafiProgram(program: String)

abstract class AbstractScafiProgramTest(
    private val knowledgePaths: List[String],
    private val promptsFilePath: String,
    private val loader: CodeGeneratorService = GeminiService.flash(GeminiService.Version.V2_0),
    private val runs: Int = 20,
    private val raw: Boolean = false,
) extends AsyncFlatSpec,
      Matchers:

  private lazy val candidatePrompts =
    decode[Prompts](Source.fromResource(promptsFilePath).mkString) match
      case Right(prompts) => prompts
      case Left(error) => throw new RuntimeException(s"Failed to decode prompts $error")

  private def programSpecification(knowledge: String, promptSpecification: String): Future[ScafiProgram] = if !raw then
    loader.generateMain(knowledge, promptSpecification).map(ScafiProgram(_))
  else loader.generateRaw(knowledge, "", promptSpecification).map(ScafiProgram(_))

  private def executeScafiProgram(
      programUnderTest: ScafiProgram,
      preamble: String,
      post: String,
      fileName: Option[String] = None,
  ): Network =
    val builtProgram = buildProgram(programUnderTest.program, preamble, post)
    fileName.foreach(writeToFile(AbstractScafiProgramTest.folderName, _, builtProgram))
    Try { executeFromString[Network](builtProgram) } match
      case Success(producedNet) => producedNet
      case Failure(exception) =>
        fail(s"Failed to execute program ${programUnderTest.program}", exception)

  private def writeToFile(path: Path, fileName: String, content: String): Path =
    Files.write(path.resolve(fileName), content.getBytes)

  def baselineWorkingProgram(): String

  def programTests(producedNet: Network): Assertion

  def postAction(): String = ""

  def preAction(): String = ""

  def testCase: String

  // Baseline verification from Scafi
  it should s"$testCase [synthetic test]" in:
    programTests(executeScafiProgram(ScafiProgram(baselineWorkingProgram()), preAction(), postAction()))

  for
    n <- 0 until runs
    prompt <- candidatePrompts.prompts
    knowledgeFile <- knowledgePaths
  do
    Using(Source.fromResource(knowledgeFile)): source =>
      val testName =
        s"$testCase with knowledge $knowledgeFile @ round-$n-prompt${candidatePrompts.prompts.indexOf(prompt)}"

      val fileName =
        s"$testCase-round-$n-${candidatePrompts.prompts.indexOf(prompt)}.scala"
      val knowledge = source.mkString
      // Check if the provided program is correct as the synthetic test program
      it should testName in:
        programSpecification(knowledge, prompt).map(program =>
          programTests(executeScafiProgram(program, preAction(), postAction(), Some(fileName))),
        )
end AbstractScafiProgramTest

object AbstractScafiProgramTest:
  private lazy val folderName =
    val folder = Path.of("data", "generated")
    folder
