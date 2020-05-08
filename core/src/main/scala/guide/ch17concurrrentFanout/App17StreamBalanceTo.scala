package guide.ch17concurrrentFanout

import scala.concurrent.ExecutionContext

import cats.effect.ContextShift
import cats.effect.IO
import fs2.Pipe
import fs2.Stream

object App17StreamBalanceTo extends hutil.App {

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  val stream: Stream[IO, Int] =
    Stream.range(1, 11).covary[IO]

  val pipe: Pipe[IO, Int, Unit] =
    worker =>
      worker
        .evalMap { o => IO(println(s">> ?: ${o.toString}")) }

  val joined: Stream[IO, Unit] = stream.balanceTo(chunkSize = 2, maxConcurrent = 3)(pipe)
  joined.compile.drain.unsafeRunSync
}
