import sbt._

object Dependencies {

  import Versions._

  lazy val collectionCompat          = "org.scala-lang.modules" %% "scala-collection-compat" % collectionCompatVersion
  lazy val shapeless                 = "com.chuusai"            %% "shapeless"               % shapelessVersion
  lazy val fs2WithCE2Core            = "co.fs2"                 %% "fs2-core"                % fs2WithCE2Version
  lazy val fs2WithCE2Io              = "co.fs2"                 %% "fs2-io"                  % fs2WithCE2Version
  lazy val fs2WithCE2ReactiveStreams = "co.fs2"                 %% "fs2-reactive-streams"    % fs2WithCE2Version
  lazy val fs2WithCE3Core            = "co.fs2"                 %% "fs2-core"                % fs2WithCE3Version
  lazy val fs2WithCE3Io              = "co.fs2"                 %% "fs2-io"                  % fs2WithCE3Version
  lazy val fs2WithCE3ReactiveStreams = "co.fs2"                 %% "fs2-reactive-streams"    % fs2WithCE3Version
  lazy val fs2WithCE3Scodec          = "co.fs2"                 %% "fs2-scodec"              % fs2WithCE3Version
  lazy val monixWithCE2Eval          = "io.monix"               %% "monix-eval"              % monixWithCE2Version
  lazy val monixWithCE3Eval          = "io.monix"               %% "monix-eval"              % monixWithCE3Version
  lazy val ip4s                      = "com.comcast"            %% "ip4s-core"               % ip4sVersion
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
