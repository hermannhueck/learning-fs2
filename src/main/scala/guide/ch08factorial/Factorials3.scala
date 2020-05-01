package guide.ch08factorial

import java.nio.file.{Paths, StandardOpenOption}

import cats.effect.{ContextShift, IO}
import fs2.{io, text, Stream}

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContextExecutorService
import java.util.concurrent.Executors
import cats.effect.Blocker

object Factorials3 extends App {

  println("\n=====")

  val blockingEC: ExecutionContextExecutorService =
    ExecutionContext.fromExecutorService(Executors.newCachedThreadPool)
  val blocker = Blocker.liftExecutionContext(blockingEC)

  val ec: ExecutionContext          = ExecutionContext.global
  implicit val cs: ContextShift[IO] = IO.contextShift(ec)

  val ints: Stream[IO, Int] = Stream.range(1, 31).covary[IO]
  val factorials: Stream[IO, BigInt] =
    ints.scan(BigInt(1))((acc, next) => acc * next)

  val stream: Stream[IO, Unit] =
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

  stream.compile.drain.unsafeRunSync()
  blockingEC.shutdown

  println("=====\n")
}
