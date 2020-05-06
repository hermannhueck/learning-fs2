package guide.ch17concurrrentFanout

import cats.effect.{ContextShift, IO}
import fs2.Stream

import scala.concurrent.ExecutionContext
import java.util.concurrent.atomic.AtomicInteger

object App12cStreamBalanceAvailable extends hutil.App {

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  val stream: Stream[IO, Int] =
    Stream.range(1, 11).covary[IO]

  val workerNum = new AtomicInteger(0)

  val streamOfStreams: Stream[IO, Stream[IO, Unit]] =
    stream
      .rechunkRandomly(2.0, 4.0)
      .balanceAvailable // uses unlimited chunkSize
      .map { worker: Stream[IO, Int] =>
        // same as: stream.through(Broadcast(minReady = 1)).map { worker: Stream[IO, Int] =>
        worker
          .evalMap { o => IO(println(s">> ${workerNum.getAndIncrement()}: ${o.toString}")) }
      }

  val joined: Stream[IO, Unit] = streamOfStreams.take(3).parJoinUnbounded
  joined.compile.drain.unsafeRunSync
}
