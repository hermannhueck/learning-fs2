import Dependencies._

name := "learning-fs2"

lazy val root = (project in file(".")).settings(
  inThisBuild(
    List(
      version := "1.0.0",
      scalaVersion := "2.13.2",
      scalacOptions ++= Seq(
        "-encoding",
        "UTF-8",        // source files are in UTF-8
        "-deprecation", // warn about use of deprecated APIs
        "-unchecked",   // warn about unchecked type parameters
        "-feature"      // warn about misused language features
        //"-Xlint",                 // enable handy linter warnings
        //"-Xfatal-warnings",        // turn compiler warnings into errors
      ),
      libraryDependencies ++= Seq(
        Libraries.fs2Core,
        Libraries.fs2Io,
        Libraries.fs2ReactiveStreams,
        Libraries.monixEval,
        compilerPlugin(Libraries.kindProjector)
      ),
      initialCommands := s"""
        import fs2._, cats.effect._, cats.effect.implicits._, cats.implicits._
        import scala.concurrent.ExecutionContext.Implicits.global
        import scala.concurrent.duration._
        implicit val contextShiftIO: ContextShift[IO] = IO.contextShift(global)
        implicit val timerIO: Timer[IO] = IO.timer(global)
      """ // initialize REPL
    )
  )
)
