package guide.ch08factorial

import java.nio.file.{Paths, StandardOpenOption}

import cats.effect.{ContextShift, ExitCode, IO, IOApp}
import fs2.{io, text, Stream}

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContextExecutorService
import java.util.concurrent.Executors
import cats.effect.Blocker

object Factorials5 extends IOApp {

  val ints: Stream[IO, Int] = Stream.range(1, 31).covary[IO]
  val factorials: Stream[IO, BigInt] =
    ints.scan(BigInt(1))((acc, next) => acc * next)

  val stream: Stream[IO, Unit] =
    Stream.resource(Blocker[IO]).flatMap { blocker =>
      factorials
        .zipWithIndex
        .map { case num -> index => s"$index = $num\n" }
        .through(text.utf8Encode)
        .through(
          io.file
            .writeAll(
              Paths.get("output/factorials-fs2.txt"),
              blocker,
              flags = List(StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
            )
        )
    }

  def run(args: List[String]): IO[ExitCode] =
    for {
      _ <- IO(println("\n====="))
      _ <- stream.compile.drain
      _ <- IO(println("=====\n"))
    } yield ExitCode.Success
}
