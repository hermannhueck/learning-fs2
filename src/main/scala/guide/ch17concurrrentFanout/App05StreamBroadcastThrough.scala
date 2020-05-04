package guide.ch17concurrrentFanout

import cats.effect.{ContextShift, IO}
import fs2.{Pipe, Stream}

import scala.concurrent.ExecutionContext

object App05StreamBroadcastThrough extends hutil.App {

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  val stream: Stream[IO, Int] =
    Stream(1, 2, 3, 4).covary[IO]

  val pipe: Pipe[IO, Int, Unit] =
    worker =>
      worker
        .evalMap { o => IO(println(s">> ?: ${o.toString}")) }

  val joined: Stream[IO, Unit] = stream.broadcastThrough(maxConcurrent = 3)(pipe)
  joined.compile.drain.unsafeRunSync
}
