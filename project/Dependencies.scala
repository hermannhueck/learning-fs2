import sbt._

object Dependencies {

  import Versions._

  lazy val collectionCompat          = "org.scala-lang.modules" %% "scala-collection-compat" % collectionCompatVersion
  lazy val shapeless                 = "com.chuusai"            %% "shapeless"               % shapelessVersion
  lazy val fs2withCE2Core            = "co.fs2"                 %% "fs2-core"                % fs2WithCE2Version
  lazy val fs2withCE2Io              = "co.fs2"                 %% "fs2-io"                  % fs2WithCE2Version
  lazy val fs2withCE2ReactiveStreams = "co.fs2"                 %% "fs2-reactive-streams"    % fs2WithCE2Version
  lazy val monixEval                 = "io.monix"               %% "monix-eval"              % monixVersion
  lazy val munit                     = "org.scalameta"          %% "munit"                   % munitVersion
  lazy val scalaCheck                = "org.scalacheck"         %% "scalacheck"              % scalaCheckVersion

  lazy val kindProjectorVersion    = "0.13.2"
  lazy val betterMonadicForVersion = "0.3.1"

  // https://github.com/typelevel/kind-projector
  lazy val kindProjectorPlugin    = compilerPlugin(
    compilerPlugin("org.typelevel" % "kind-projector" % kindProjectorVersion cross CrossVersion.full)
  )
  // https://github.com/oleg-py/better-monadic-for
  lazy val betterMonadicForPlugin = compilerPlugin(
    compilerPlugin("com.olegpy" %% "better-monadic-for" % betterMonadicForVersion)
  )
}
