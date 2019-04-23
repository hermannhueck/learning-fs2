import sbt._

object Dependencies {

  object Versions {

    val fs2Version = "1.0.+"
    val monixVersion = "3.0.0-RC2"
  }

  object Libraries {

    import Versions._

    lazy val fs2Core = "co.fs2" %% "fs2-core" % fs2Version withSources() withJavadoc()
    lazy val fs2Io = "co.fs2" %% "fs2-io" % fs2Version withSources() withJavadoc()
    lazy val fs2ReactiveStreams = "co.fs2" %% "fs2-reactive-streams" % fs2Version withSources() withJavadoc()
    lazy val monixEval = "io.monix" %% "monix-eval" % monixVersion withSources() withJavadoc()
  }
}
