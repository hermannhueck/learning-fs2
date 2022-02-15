package guide.ch08factorial

import java.nio.file.{Paths, StandardOpenOption}

import scala.concurrent.ExecutionContext

import cats.effect.{Blocker, ContextShift, IO}
import fs2.{io, text, Stream}

object Factorials4 extends hutil.App {

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  val ints: Stream[IO, Int] = Stream.range(1, 31).covary[IO]
  val factorials: Stream[IO, BigInt] =
    ints.scan(BigInt(1))((acc, next) => acc * next)

  val stream: Stream[IO, Unit] =
    Stream.resource(Blocker[IO]).flatMap { blocker =>
      factorials
        .zipWithIndex
        .map { case (num, index) => s"$index = $num\n" }
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

  stream.compile.drain.unsafeRunSync()
}
