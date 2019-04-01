name := "learning-fs2"

version := "1.0.0"

scalaVersion := "2.12.8"

val fs2Version = "1.0.4"

scalacOptions ++= Seq(
  "-encoding", "UTF-8",     // source files are in UTF-8
  "-deprecation",           // warn about use of deprecated APIs
  "-unchecked",             // warn about unchecked type parameters
  "-feature",               // warn about misused language features
  "-language:higherKinds",  // suppress warnings when using higher kinded types
  "-Ypartial-unification",  // allow the compiler to unify type constructors of different arities
  //"-Xlint",                 // enable handy linter warnings
  //"-Xfatal-warnings",        // turn compiler warnings into errors
)

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.9")

libraryDependencies ++= Seq(
  "co.fs2" %% "fs2-core" % fs2Version withSources() withJavadoc(),
  "co.fs2" %% "fs2-io" % fs2Version withSources() withJavadoc(),
  "co.fs2" %% "fs2-reactive-streams" % fs2Version withSources() withJavadoc(),
  "io.monix" %% "monix-eval" % "3.0.0-RC2" withSources() withJavadoc(),
)

/*
libraryDependencies += {
  "com.lihaoyi" % "ammonite" % "1.6.3" % "test" cross CrossVersion.full
}

sourceGenerators in Test += Def.task {
  val file = (sourceManaged in Test).value / "amm.scala"
  IO.write(file, """object amm extends App { ammonite.Main.main(args) }""")
  Seq(file)
}.taskValue

// Optional, required for the `source` command to work
(fullClasspath in Test) ++= {
  (updateClassifiers in Test).value
    .configurations
    .find(_.configuration == Test.name)
    .get
    .modules
    .flatMap(_.artifacts)
    .collect{case (a, f) if a.classifier == Some("sources") => f}
}

addCommandAlias("amm", s"test:runMain amm --predef ammonite-init.sc")
*/

// initialize REPL
initialCommands := s"""
    import fs2._, cats.effect._, cats.effect.implicits._, cats.implicits._
    import scala.concurrent.ExecutionContext.Implicits.global
    import scala.concurrent.duration._
    import scala.language.higherKinds
    implicit val contextShiftIO: ContextShift[IO] = IO.contextShift(global)
    implicit val timerIO: Timer[IO] = IO.timer(global)
  """
