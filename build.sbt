val scala3Version = "3.6.2"

ThisBuild / scalaVersion := scala3Version
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
ThisBuild / scalacOptions ++= Seq(
  "-Werror",
  "-Wunused:all",
  "-Wvalue-discard",
  "-Wnonunit-statement",
//  "-Yexplicit-nulls",
  "-Wsafe-init",
  "-Ycheck-reentrant",
  "-Xcheck-macros",
  "-rewrite",
  "-indent",
  "-unchecked",
  "-explain",
  "-feature",
//  "-language:strictEquality",
  "-language:implicitConversions"
)
ThisBuild / coverageEnabled := true
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision
ThisBuild / libraryDependencies ++= Seq(
  "org.scalactic" %% "scalactic" % "3.2.19",
  "org.scalatest" %% "scalatest" % "3.2.19" % Test,
  "com.vladsch.flexmark" % "flexmark-all" % "0.64.8" % Test,
  "it.unibo.scafi" %% "scafi-core" % "1.6.0",
  "it.unibo.scafi" %% "scafi-simulator" % "1.6.0",
  "org.scala-lang" %% "scala3-compiler" % scala3Version,
  "com.lihaoyi" %% "requests" % "0.9.0",
  "io.circe" %% "circe-core" % "0.14.10",
  "io.circe" %% "circe-generic" % "0.14.10",
  "io.circe" %% "circe-parser" % "0.14.10",
  "com.softwaremill.retry" %% "retry" % "0.3.6",
  "io.github.ollama4j" % "ollama4j" % "1.0.93"
)
ThisBuild / Test / fork := true
ThisBuild / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-h", "target/test-reports")

lazy val root = project
  .in(file("."))
  .settings(
    name := "experiments-2025-acm-iot-ac-llm",
    sonatypeCredentialHost := "s01.oss.sonatype.org",
    sonatypeRepository := "https://s01.oss.sonatype.org/service/local",
    sonatypeProfileName := "it.nicolasfarabegoli",
  )
