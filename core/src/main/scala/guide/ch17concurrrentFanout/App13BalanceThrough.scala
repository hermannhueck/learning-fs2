package guide.ch17concurrrentFanout

import scala.concurrent.ExecutionContext

import cats.effect.ContextShift
import cats.effect.IO
import fs2.Pipe
import fs2.Stream
import fs2.concurrent.Balance

object App13BalanceThrough extends hutil.App {

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  val stream: Stream[IO, Int] =
    Stream.range(1, 11).covary[IO]

  def pipe(num: Int): Pipe[IO, Int, Unit] =
    worker =>
      worker
        .evalMap { o => IO(println(s">> $num: ${o.toString}")) }

  val pipes = Seq(pipe(1), pipe(2), pipe(3))

  val pipeOfPipes: Pipe[IO, Int, Unit] = Balance.through(chunkSize = 2)(pipes: _*)

  val joined: Stream[IO, Unit] = stream.through(pipeOfPipes)
  joined.compile.drain.unsafeRunSync
}
