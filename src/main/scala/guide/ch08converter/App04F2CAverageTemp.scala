package guide.ch08converter

import java.nio.file.{Path, Paths}

import cats.effect.{ExitCode, IO, IOApp}
import fs2.{io, text, Stream}

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContextExecutorService
import java.util.concurrent.Executors
import cats.effect.Blocker

object App04F2CAverageTemp extends IOApp {

  val blockingEC: ExecutionContextExecutorService =
    ExecutionContext.fromExecutorService(Executors.newCachedThreadPool)
  val blocker = Blocker.liftExecutionContext(blockingEC)

  private val input: Path = Paths.get("testdata/fahrenheit.txt")
  private val output      = Paths.get("output/celsius-fs2.txt")

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
        case ((accTemp, accCount), temp) => (accTemp + temp, accCount + 1)
      }
      .map {
        case (temp, count) => if (count == 0L) None else Some(temp / count)
      }

  def run(args: List[String]): IO[ExitCode] = {
    val io: IO[List[Option[Double]]] = converter.compile.toList
    val average: Option[Double]      = io.unsafeRunSync().head
    val text                         = average.map(_.toString).getOrElse("no temperatures provided")
    println(s"\nAverage Temperature (Celsius):  $text\n")
    IO(ExitCode.Success)
  }
}
