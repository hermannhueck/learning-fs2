package guide.ch08converter

import java.nio.file.{Path, Paths}
import java.util.concurrent.Executors

import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService}

import cats.effect.{Blocker, ContextShift, IO}
import fs2.{Stream, io, text}

object App02F2CReadPrint extends hutil.App {

  val blockingEC: ExecutionContextExecutorService =
    ExecutionContext.fromExecutorService(Executors.newCachedThreadPool)
  val blocker = Blocker.liftExecutionContext(blockingEC)

  implicit private val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  private val input: Path = Paths.get("testdata/fahrenheit.txt")

  def fahrenheitToCelsius(f: Double): Double =
    (f - 32.0) * (5.0 / 9.0)

  val converter: Stream[IO, Unit] =
    io.file
      .readAll[IO](input, blocker, 4096)
      .through(text.utf8Decode)
      .through(text.lines)
      .filter(s => !s.trim.isEmpty && !s.startsWith("//"))
      .map(line => fahrenheitToCelsius(line.toDouble).toString)
      // .map { line => println(line); line }
      .lines(java.lang.System.out)

  // this also works for large files which don't fit into memory

  val ioUnit: IO[Unit] = converter.compile.drain
  ioUnit.unsafeRunSync()

  blockingEC.shutdown
}
