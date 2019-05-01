package guide.ch08converter

import java.nio.file.{Path, Paths}
import java.util.concurrent.Executors

import cats.effect.{ExitCode, IO, IOApp, Resource}
import cats.implicits._
import fs2.{Stream, io, text}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService}

/*
  Step-by-step explanation at:
  https://github.com/functional-streams-for-scala/fs2/blob/series/1.0/docs/src/ReadmeExample.md
  and at:
  https://www.youtube.com/watch?v=cahvyadYfX8
 */
object App07F2CWithBlockingECResource extends IOApp {

  def blockingExecutionContext: ExecutionContextExecutorService =
    ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(2))

  private val blockingECResource: Resource[IO, ExecutionContextExecutorService] =
    Resource.make(IO(blockingExecutionContext))(ec => IO(ec.shutdown()))

  private val input: Path = Paths.get("testdata/fahrenheit.txt")
  private val output = Paths.get("testdata/celsius.txt")

  def fahrenheitToCelsius(f: Double): Double =
    (f - 32.0) * (5.0 / 9.0)

  def convert(ec: ExecutionContext): Stream[IO, Unit] =
    io.file.readAll[IO](input, ec, 4096)
      .through(text.utf8Decode)
      .through(text.lines)
      .filter(s => !s.trim.isEmpty && !s.startsWith("//"))
      .map(line => fahrenheitToCelsius(line.toDouble).toString)
      // .map { line => println(line); line }
      .intersperse("\n")
      .through(text.utf8Encode)
      .through(io.file.writeAll(output, ec)) // ++ Stream.eval[IO, Unit](IO { throw new IllegalStateException("illegal state")} )

  val converter: Stream[IO, Unit] = Stream.resource(blockingECResource)
    .flatMap { ec => convert(ec) }

  def run(args: List[String]): IO[ExitCode] =
    converter.compile.drain.as {
      println(s"\nFahrenheit from $input converted to Celsius in $output\n")
      ExitCode.Success
    }
}
