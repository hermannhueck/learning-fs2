import Versions._
import Dependencies._
import ScalacOptions._

val projectName        = "learning-fs2"
val projectDescription = "Learning functional streaming using fs2"

ThisBuild / fork                   := true
ThisBuild / turbo                  := true // default: false
ThisBuild / includePluginResolvers := true // default: false
Global / onChangedBuildSource      := ReloadOnSourceChanges

inThisBuild(
  Seq(
    version                  := projectVersion,
    scalaVersion             := scala2Version,
    publish / skip           := true,
    scalacOptions ++= defaultScalacOptions,
    semanticdbEnabled        := true,
    semanticdbVersion        := scalafixSemanticdb.revision,
    scalafixDependencies ++= Seq("com.github.liancheng" %% "organize-imports" % "0.3.0"),
    Test / parallelExecution := false,
    // run 100 tests for each property // -s = -minSuccessfulTests
    Test / testOptions += Tests.Argument(TestFrameworks.ScalaCheck, "-s", "100"),
    initialCommands          :=
      s"""|
          |import scala.util.chaining._
          |import fs2._, cats.effect._, cats.effect.implicits._, cats.implicits._
          |import scala.concurrent.ExecutionContext.Implicits.global
          |import scala.concurrent.duration._
          |implicit val contextShiftIO: ContextShift[IO] = IO.contextShift(global)
          |implicit val timerIO: Timer[IO] = IO.timer(global)
          |println
          |""".stripMargin // initialize REPL
  )
)

lazy val root = (project in file("."))
  .aggregate(coreCE2)
  .aggregate(coreCE3)
  .settings(
    name                              := "root",
    description                       := "root project",
    Compile / console / scalacOptions := consoleScalacOptions
    // sourceDirectories := Seq.empty
  )

lazy val hutil = (project in file("hutil"))
  .settings(
    name                              := "hutil",
    description                       := "Hermann's Utilities",
    Compile / console / scalacOptions := consoleScalacOptions,
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-compiler" % scalaVersion.value,
      "org.scala-lang" % "scala-reflect"  % scalaVersion.value,
      kindProjectorPlugin,
      betterMonadicForPlugin
    )
  )

lazy val coreCE2 = (project in file("coreCE2"))
  .dependsOn(hutil)
  .settings(
    name                              := projectName,
    description                       := projectDescription,
    Compile / console / scalacOptions := consoleScalacOptions,
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-compiler" % scalaVersion.value,
      "org.scala-lang" % "scala-reflect"  % scalaVersion.value,
      shapeless,
      fs2WithCE2Core,
      fs2WithCE2Io,
      fs2WithCE2ReactiveStreams,
      monixWithCE2Eval,
      munit,
      kindProjectorPlugin,
      betterMonadicForPlugin
    ) ++ Seq(
      scalaCheck
    ).map(_            % Test)
  )

lazy val coreCE3 = (project in file("coreCE3"))
  .dependsOn(hutil)
  .settings(
    name                              := projectName,
    description                       := projectDescription,
    Compile / console / scalacOptions := consoleScalacOptions,
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-compiler" % scalaVersion.value,
      "org.scala-lang" % "scala-reflect"  % scalaVersion.value,
      shapeless,
      fs2WithCE3Core,
      fs2WithCE3Io,
      fs2WithCE3ReactiveStreams,
      munit,
      kindProjectorPlugin,
      betterMonadicForPlugin
    ) ++ Seq(
      scalaCheck
    ).map(_            % Test)
  )

// GraphBuddy support
// resolvers += Resolver.bintrayRepo("virtuslab", "graphbuddy")
// addCompilerPlugin("com.virtuslab.semanticgraphs" % "scalac-plugin" % "0.0.10" cross CrossVersion.full)
// scalacOptions += "-Yrangepos"
