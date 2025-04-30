val scala3Version = "3.6.2"
val scala2Version = "2.13.16"


ThisBuild / organization := "it.nicolasfarabegoli"
ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"
ThisBuild / homepage := Some(
  url(
    "https://github.com/nicolasfara/Template-for-Scala-Multiplatform-Projects"
  )
)
ThisBuild / licenses := List(
  "Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0")
)
ThisBuild / versionScheme := Some("early-semver")
ThisBuild / developers := List(
  Developer(
    "nicolasfara",
    "Nicolas Farabegoli",
    "nicolas.farabegoli@gmail.com",
    url("https://nicolasfarabegoli.it")
  )
)
//ThisBuild / coverageEnabled := true
//ThisBuild / semanticdbEnabled := true
//ThisBuild / semanticdbVersion := scalafixSemanticdb.revision
ThisBuild / fork := true
//ThisBuild / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-h", "target/test-reports")

val langchain4jVersion = "1.0.0-beta3"
val circeVersion = "0.14.10"
lazy val root = project
  .in(file("."))
  .settings(
    scalaVersion := scala3Version,
    name := "experiments-2025-acm-iot-ac-llm",
    sonatypeCredentialHost := "s01.oss.sonatype.org",
    sonatypeRepository := "https://s01.oss.sonatype.org/service/local",
    sonatypeProfileName := "it.nicolasfarabegoli",
    libraryDependencies ++= Seq(
      "org.scalactic" %% "scalactic" % "3.2.19",
      "org.scalatest" %% "scalatest" % "3.2.19" % Test,
      "com.vladsch.flexmark" % "flexmark-all" % "0.64.8" % Test,
      "it.unibo.scafi" %% "scafi-core" % "1.6.0",
      "it.unibo.scafi" %% "scafi-simulator" % "1.6.0",
      "org.scala-lang" %% "scala3-compiler" % scala3Version,
      "com.lihaoyi" %% "requests" % "0.9.0",
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "com.softwaremill.retry" %% "retry" % "0.3.6",
      "io.github.ollama4j" % "ollama4j" % "1.0.100",
      "com.lihaoyi" %% "os-lib" % "0.11.4",
      "dev.langchain4j" % "langchain4j-google-ai-gemini" % langchain4jVersion,
      "dev.langchain4j" % "langchain4j-embeddings-all-minilm-l6-v2" % langchain4jVersion,
      "dev.langchain4j" % "langchain4j-embeddings-all-minilm-l6-v2-q" % langchain4jVersion,
      "dev.langchain4j" % "langchain4j-embeddings-bge-small-en-v15" % langchain4jVersion,
      "dev.langchain4j" % "langchain4j-open-ai" % langchain4jVersion,
      "dev.langchain4j" % "langchain4j-ollama" % langchain4jVersion,
      "dev.langchain4j" % "langchain4j-github-models" % langchain4jVersion,
      "dev.langchain4j" % "langchain4j-local-ai" % langchain4jVersion,
      "dev.langchain4j" % "langchain4j-community-xinference" % langchain4jVersion,
      "org.slf4j"       % "slf4j-api"      % "2.0.17",
      "ch.qos.logback"  % "logback-classic" % "1.5.18",
      "org.typelevel" %% "cats-effect" % "3.6.1",
      "org.typelevel" %% "log4cats-core"    % "2.7.0",  // Only if you want to Support Any Backend
      "org.typelevel" %% "log4cats-slf4j"   % "2.7.0",  // Direct Slf4j Support - Recommended
    ),
    scalacOptions ++= Seq(
      "-Werror",
      "-Wunused:all",
      "-Wvalue-discard",
      "-Wnonunit-statement",
      //  "-Yexplicit-nulls",
      "-Wsafe-init",
//      "-Ycheck-reentrant",
      "-Xcheck-macros",
      "-rewrite",
      "-indent",
      "-unchecked",
      "-explain",
      "-feature",
      //  "-language:strictEquality",
      "-language:implicitConversions"
    )
  )

lazy val simulation = project
  .in(file("simulation"))
  .settings(
    scalaVersion := scala2Version,
    name := "simulation",
    libraryDependencies ++= Seq(
      "it.unibo.alchemist" % "alchemist" % "39.0.1",
      "it.unibo.alchemist" % "alchemist-incarnation-scafi" % "39.0.1",
      "it.unibo.alchemist" % "alchemist-swingui" % "39.0.1",
      "it.unibo.alchemist" % "alchemist-implementationbase" % "39.0.1",
    )
  )
