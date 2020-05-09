package guide.ch08converter

import java.nio.file.{Path, Paths}
import java.util.concurrent.Executors

import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService}

import cats.effect.{Blocker, ExitCode, IO}
import fs2.{Stream, io, text}

object App05F2CReadWrite extends hutil.IOApp {

  val blockingEC: ExecutionContextExecutorService =
    ExecutionContext.fromExecutorService(Executors.newCachedThreadPool)
  val blocker = Blocker.liftExecutionContext(blockingEC)

  private val input: Path = Paths.get("testdata/fahrenheit.txt")
  private val output      = Paths.get("testdata/celsius.txt")

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
      .intersperse("\n")
      .through(text.utf8Encode)
      .through(io.file.writeAll(output, blocker))

  def ioRun(args: List[String]): IO[ExitCode] = {
    converter.compile.drain
    blockingEC.shutdown
    IO(ExitCode.Success)
  }
}
