package guide.ch17concurrrentFanout

import scala.concurrent.ExecutionContext

import cats.effect.{ContextShift, IO}
import fs2.concurrent.Broadcast
import fs2.{Pipe, Stream}

object App03BroadcastThrough extends hutil.App {

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  val stream: Stream[IO, Int] =
    Stream(1, 2, 3, 4).covary[IO]

  def pipe(num: Int): Pipe[IO, Int, Unit] =
    worker =>
      worker
        .evalMap { o => IO(println(s">> $num: ${o.toString}")) }

  val pipes = Seq(pipe(1), pipe(2), pipe(3))

  val pipeOfPipes: Pipe[IO, Int, Unit] = Broadcast.through(pipes: _*)

  val joined: Stream[IO, Unit] = stream.through(pipeOfPipes)
  joined.compile.drain.unsafeRunSync
}
