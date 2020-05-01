package guide.ch08converter

import java.nio.file.{Path, Paths}
import java.util.concurrent.Executors

import cats.effect._
import cats.syntax.functor._
import fs2.{io, text, Stream}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService}
import scala.language.higherKinds

/*
  Step-by-step explanation at:
  https://github.com/functional-streams-for-scala/fs2/blob/series/1.0/docs/src/ReadmeExample.md
  and at:
  https://www.youtube.com/watch?v=cahvyadYfX8
 */
object App09F2CGeneric extends IOApp {

  val input: Path  = Paths.get("testdata/fahrenheit.txt")
  val output: Path = Paths.get("testdata/celsius.txt")

  val blockingEC: ExecutionContextExecutorService =
    ExecutionContext.fromExecutorService(Executors.newCachedThreadPool)

  def fahrenheitToCelsius(f: Double): Double =
    (f - 32.0) * (5.0 / 9.0)

  def convert[F[_]: Sync: ContextShift](blocker: Blocker): Stream[F, Unit] =
    io.file
      .readAll(input, blocker, 4096)
      .through(text.utf8Decode)
      .through(text.lines)
      .filter(s => !s.trim.isEmpty && !s.startsWith("//"))
      .map(line => fahrenheitToCelsius(line.toDouble).toString)
      .intersperse("\n")
      .through(text.utf8Encode)
      .through(
        io.file.writeAll(output, blocker)
      )

  def converter[F[_]: Sync: ContextShift]: Stream[F, Unit] =
    Stream
      .bracket(Sync[F].delay(blockingEC))(ec => Sync[F].delay(blockingEC.shutdown()))
      .flatMap { blockingEC => convert(Blocker.liftExecutionContext(blockingEC)) }

  def run(args: List[String]): IO[ExitCode] =
    converter[IO].compile.drain.as {
      println(s"\nFahrenheit from $input converted to Celsius in $output\n")
      ExitCode.Success
    }
}
