package guide.ch08converter

import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.Executors

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContextExecutorService

import cats.effect.Blocker
import cats.effect.ExitCode
import cats.effect.IO
import fs2.Stream
import fs2.io
import fs2.text

/*
  Step-by-step explanation at:
  https://github.com/functional-streams-for-scala/fs2/blob/series/1.0/docs/src/ReadmeExample.md
  and at:
  https://www.youtube.com/watch?v=cahvyadYfX8
 */
object App06F2CWithBlockingEC extends hutil.IOApp {

  val blockingEC: ExecutionContextExecutorService =
    ExecutionContext.fromExecutorService(Executors.newCachedThreadPool)
  val blocker = Blocker.liftExecutionContext(blockingEC)

  private val input: Path = Paths.get("testdata/fahrenheit.txt")
  private val output      = Paths.get("testdata/celsius.txt")

  def fahrenheitToCelsius(f: Double): Double =
    (f - 32.0) * (5.0 / 9.0)

  def convert(blocker: Blocker): Stream[IO, Unit] =
    io.file
      .readAll[IO](input, blocker, 4096)
      .through(text.utf8Decode)
      .through(text.lines)
      .filter(s => !s.trim.isEmpty && !s.startsWith("//"))
      .map(line => fahrenheitToCelsius(line.toDouble).toString)
      .intersperse("\n")
      .through(text.utf8Encode)
      .through(
        io.file.writeAll(output, blocker)
      )

  val converter: Stream[IO, Unit] = convert(blocker)

  def ioRun(args: List[String]): IO[ExitCode] = {
    converter.compile.drain
    println(s"\nFahrenheit from $input converted to Celsius in $output\n")
    blockingEC.shutdown
    IO(ExitCode.Success)
  }
}
