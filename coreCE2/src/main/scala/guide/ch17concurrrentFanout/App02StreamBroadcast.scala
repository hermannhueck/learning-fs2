package guide.ch17concurrrentFanout

import scala.concurrent.ExecutionContext

import cats.effect.{ContextShift, IO}
import fs2.Stream

object App02StreamBroadcast extends hutil.App {

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  val stream: Stream[IO, Int] =
    Stream(1, 2, 3, 4).covary[IO]

  val streamOfStreams: Stream[IO, Stream[IO, Unit]] =
    stream.broadcast.map { worker: Stream[IO, Int] =>
      // same as: stream.through(Broadcast(minReady = 1)).map { worker: Stream[IO, Int] =>
      worker
        .evalMap { o => IO(println(s">> ?: ${o.toString}")) }
    }

  val joined: Stream[IO, Unit] = streamOfStreams.take(3).parJoinUnbounded
  joined.compile.drain.unsafeRunSync()
}
