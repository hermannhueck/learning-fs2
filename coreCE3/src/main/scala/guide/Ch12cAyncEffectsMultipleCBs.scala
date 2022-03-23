package guide

import fs2._
import cats.effect._
import cats.effect.std.{Dispatcher, Queue}
import scala.concurrent.duration._

object Ch12cAyncEffectsMultipleCBs extends IOApp.Simple {

  type Row        = List[String]
  type RowOrError = Either[Throwable, Row]

  trait CSVHandle {
    def withRows(cb: RowOrError => Unit): Unit
  }

  val handle = new CSVHandle {
    def withRows(cb: RowOrError => Unit): Unit = {
      Thread.sleep(200)
      cb(Right(List("a", "b", "c")))
      cb(Left(new Exception("boom")))
    }
  }

  def rows[F[_]](h: CSVHandle)(implicit F: Async[F]): Stream[F, RowOrError] = {
    for {
      dispatcher: Dispatcher[F]       <- Stream.resource(Dispatcher[F])
      q: Queue[F, Option[RowOrError]] <- Stream.eval(Queue.unbounded[F, Option[RowOrError]])
      _: Unit                         <- Stream.eval {
                                           F.delay {
                                             def enqueue(v: Option[RowOrError]): Unit = dispatcher.unsafeRunAndForget(q.offer(v))

                                             // Fill the data - withRows blocks while reading the file, asynchronously invoking the callback we pass to it on every row
                                             h.withRows(e => enqueue(Some(e)))
                                             // Upon returning from withRows, signal that our stream has ended.
                                             enqueue(None)
                                           }
                                         }
      // Due to `fromQueueNoneTerminated`, the stream will terminate when it encounters a `None` value
      row: RowOrError                 <- Stream.fromQueueNoneTerminated(q) // .rethrow
    } yield row
  }

  val stream: Stream[IO, RowOrError] = rows[IO](handle)

  val run: IO[Unit] = for {
    _ <- IO.println("-----------------------------------")
    _ <- stream.evalMap(IO.println).compile.drain
    _ <- IO.sleep(200.millis)
    _ <- IO.println("-----------------------------------")
  } yield ()
}
