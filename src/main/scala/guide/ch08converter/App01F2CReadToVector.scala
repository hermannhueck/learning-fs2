package guide.ch08converter

import java.nio.file.{Path, Paths}

import cats.effect.{ContextShift, ExitCode, IO, IOApp}
import fs2.{io, text, Stream}

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContextExecutorService
import java.util.concurrent.Executors
import cats.effect.Blocker

object App01F2CReadToVector extends hutil.App {

  val blockingEC: ExecutionContextExecutorService =
    ExecutionContext.fromExecutorService(Executors.newCachedThreadPool)
  val blocker = Blocker.liftExecutionContext(blockingEC)

  implicit private val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  private val input: Path = Paths.get("testdata/fahrenheit.txt")

  def fahrenheitToCelsius(f: Double): Double =
    (f - 32.0) * (5.0 / 9.0)

  val converter: Stream[IO, String] =
    io.file
      .readAll[IO](input, blocker, 4096)
      .through(text.utf8Decode)
      .through(text.lines)
      .filter(s => !s.trim.isEmpty && !s.startsWith("//"))
      .map(line => fahrenheitToCelsius(line.toDouble).toString)

  val ioVector: IO[Vector[String]] = converter.compile.toVector
  ioVector.unsafeRunSync() foreach println

  blockingEC.shutdown
}
