package guide.ch17concurrrentFanout

import scala.concurrent.ExecutionContext

import cats.effect.{ContextShift, IO}
import fs2.Stream
import fs2.concurrent.Broadcast

object App01BroadcastApply extends hutil.App {

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  val stream: Stream[IO, Int] =
    Stream(1, 2, 3, 4).covary[IO]

  val streamOfStreams: Stream[IO, Stream[IO, Unit]] =
    stream
      .through(Broadcast(minReady = 3))
      .map { worker: Stream[IO, Int] =>
        worker
          .evalMap { o => IO(println(s">> ?: ${o.toString}")) }
      }

  streamOfStreams
    .take(3)
    .parJoinUnbounded
    .compile
    .drain
    .unsafeRunSync
}
