package guide.ch17concurrrentFanout

import scala.concurrent.ExecutionContext

import cats.effect.{ContextShift, IO}
import fs2.{Pipe, Stream}

object App06StreamBroadcastTo extends hutil.App {

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  val stream: Stream[IO, Int] =
    Stream(1, 2, 3, 4).covary[IO]

  def pipe(num: Int): Pipe[IO, Int, Unit] =
    worker =>
      worker
        .evalMap { o => IO(println(s">> $num: ${o.toString}")) }

  val pipes = Seq(pipe(1), pipe(2), pipe(3))

  val joined: Stream[IO, Unit] = stream.broadcastTo(pipes: _*)
  joined.compile.drain.unsafeRunSync()
}
