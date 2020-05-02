import sbt._

object Dependencies {

  lazy val fs2Version           = "2.3.0"
  lazy val monixVersion         = "3.2.1"
  lazy val kindProjectorVersion = "0.11.0"
  lazy val munitVersion         = "0.7.1"

  lazy val fs2Core            = "co.fs2"        %% "fs2-core"             % fs2Version
  lazy val fs2Io              = "co.fs2"        %% "fs2-io"               % fs2Version
  lazy val fs2ReactiveStreams = "co.fs2"        %% "fs2-reactive-streams" % fs2Version
  lazy val monixEval          = "io.monix"      %% "monix-eval"           % monixVersion
  lazy val munit              = "org.scalameta" %% "munit"                % munitVersion
  lazy val kindProjector      = "org.typelevel" %% "kind-projector"       % kindProjectorVersion cross CrossVersion.full

}
