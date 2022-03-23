// see: https://fs2.io/#/concurrency-primitives - FIFO

package concurrency_primitives

import scala.concurrent.duration._

import cats.effect.std.{Console, Queue}
import cats.effect.{Concurrent, IO, IOApp}
import fs2.Stream

class Buffering[F[_]: Concurrent: Console](q1: Queue[F, Int], q2: Queue[F, Int]) {

  def start: Stream[F, Unit] =
    Stream(
      Stream.range(0, 100).covary[F].foreach(q1.offer),
      Stream.repeatEval(q1.take).evalTap(n => Console[F].println(s"Pulling out $n from Queue #1")).foreach(q2.offer),
      // .map won't work here as you're trying to map a pure value with a side effect. Use `foreach` instead.
      Stream.repeatEval(q2.take).foreach(n => Console[F].println(s"Pulling out $n from Queue #2"))
    ).parJoin(3)
}

object Fifo extends IOApp.Simple {

  def run: IO[Unit] = {
    val stream = for {
      q1 <- Stream.eval(Queue.bounded[IO, Int](1))
      q2 <- Stream.eval(Queue.bounded[IO, Int](10))
      bp  = new Buffering[IO](q1, q2)
      _  <- Stream.sleep[IO](5.seconds) concurrently bp.start.drain
    } yield ()
    stream.compile.drain
  }
}
