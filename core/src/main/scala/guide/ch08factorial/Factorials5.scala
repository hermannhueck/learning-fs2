package guide.ch08factorial

import java.nio.file.Paths
import java.nio.file.StandardOpenOption

import cats.effect.Blocker
import cats.effect.ExitCode
import cats.effect.IO
import fs2.Stream
import fs2.io
import fs2.text

object Factorials5 extends hutil.IOApp {

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

  def ioRun(args: List[String]): IO[ExitCode] =
    for {
      _ <- IO(println("\n====="))
      _ <- stream.compile.drain
      _ <- IO(println("=====\n"))
    } yield ExitCode.Success
}
