package guide.ch17concurrrentFanout

import scala.concurrent.ExecutionContext

import cats.effect.ContextShift
import cats.effect.IO
import fs2.Stream

object App12aStreamBalance extends hutil.App {

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  val stream: Stream[IO, Int] =
    Stream.range(1, 11).covary[IO]

  val streamOfStreams: Stream[IO, Stream[IO, Unit]] =
    stream
      .balance(chunkSize = 2)
      .map { worker: Stream[IO, Int] =>
        // same as: stream.through(Balance(chunkSize)).map { worker: Stream[IO, Int] =>
        worker
          .evalMap { o => IO(println(s">> ?: ${o.toString}")) }
      }

  val joined: Stream[IO, Unit] = streamOfStreams.take(3).parJoinUnbounded
  joined.compile.drain.unsafeRunSync
}
