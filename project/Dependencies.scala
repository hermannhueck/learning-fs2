import sbt._

object Dependencies {

  object Versions {

    val fs2Version           = "2.3.0"
    val monixVersion         = "3.2.1"
    val kindProjectorVersion = "0.11.0"
  }

  object Libraries {

    import Versions._

    lazy val fs2Core            = "co.fs2"        %% "fs2-core"             % fs2Version
    lazy val fs2Io              = "co.fs2"        %% "fs2-io"               % fs2Version
    lazy val fs2ReactiveStreams = "co.fs2"        %% "fs2-reactive-streams" % fs2Version
    lazy val monixEval          = "io.monix"      %% "monix-eval"           % monixVersion
    lazy val kindProjector      = "org.typelevel" %% "kind-projector"       % kindProjectorVersion cross CrossVersion.full
  }
}
