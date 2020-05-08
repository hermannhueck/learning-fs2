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

object App04F2CAverageTemp extends hutil.IOApp {

  val blockingEC: ExecutionContextExecutorService =
    ExecutionContext.fromExecutorService(Executors.newCachedThreadPool)
  val blocker = Blocker.liftExecutionContext(blockingEC)

  private val input: Path = Paths.get("testdata/fahrenheit.txt")

  def fahrenheitToCelsius(f: Double): Double =
    (f - 32.0) * (5.0 / 9.0)

  val converter: Stream[IO, Option[Double]] =
    io.file
      .readAll[IO](input, blocker, 4096)
      .through(text.utf8Decode)
      .through(text.lines)
      .filter(s => !s.trim.isEmpty && !s.startsWith("//"))
      .map(line => fahrenheitToCelsius(line.toDouble))
      .fold((0.0: Double, 0L: Long)) {
        case (accTemp -> accCount) -> temp => (accTemp + temp, accCount + 1)
      }
      .map {
        case temp -> count => if (count == 0L) None else Some(temp / count)
      }

  def ioRun(args: List[String]): IO[ExitCode] = {
    val io: IO[List[Option[Double]]] = converter.compile.toList
    val average: Option[Double]      = io.unsafeRunSync().head
    val text                         = average.map(_.toString).getOrElse("no temperatures provided")
    println(s"Average Temperature (Celsius):  $text")
    blockingEC.shutdown
    IO(ExitCode.Success)
  }
}
