package guide.ch08factorial

import java.nio.file.{Paths, StandardOpenOption}

import cats.effect.{Blocker, ExitCode, IO}
import fs2.{io, text, Stream}

object Factorials5 extends hutil.IOApp {

  val outputFile = "output/factorials-fs2.txt"

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
              Paths.get(outputFile),
              blocker,
              flags = List(StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
            )
        )
    }

  def ioRun(args: List[String]): IO[ExitCode] =
    for {
      _ <- stream.compile.drain
      _ <- IO(println(s"Factorals written to $outputFile"))
    } yield ExitCode.Success
}
