name := "learning-fs2"

version := "1.0.0"

scalaVersion := "2.12.8"

val fs2Version = "1.0.2"

scalacOptions ++= Seq(
  "-encoding", "UTF-8",     // source files are in UTF-8
  "-deprecation",           // warn about use of deprecated APIs
  "-unchecked",             // warn about unchecked type parameters
  "-feature",               // warn about misused language features
  "-Ypartial-unification",  // allow the compiler to unify type constructors of different arities
  //"-Xlint",                 // enable handy linter warnings
  //"-Xfatal-warnings",        // turn compiler warnings into errors
)

libraryDependencies ++= Seq(
  "co.fs2" %% "fs2-core" % fs2Version withSources() withJavadoc(),
  "co.fs2" %% "fs2-io" % fs2Version withSources() withJavadoc(),
  "co.fs2" %% "fs2-reactive-streams" % fs2Version withSources() withJavadoc(),
)

initialCommands := s"""
    import fs2._, cats.effect._, cats.effect.implicits._, cats.implicits._
    import scala.concurrent.ExecutionContext.Implicits.global
    import scala.concurrent.duration._
    import scala.language.higherKinds
    implicit val contextShiftIO: ContextShift[IO] = IO.contextShift(global)
    implicit val timerIO: Timer[IO] = IO.timer(global)
  """

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.9")
