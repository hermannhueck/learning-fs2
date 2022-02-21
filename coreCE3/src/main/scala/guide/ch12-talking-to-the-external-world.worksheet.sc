// see: https://fs2.io/#/guide - Talking to the external world

import fs2._
import cats.effect.{Async, IO, Sync}
import cats.effect.std.{Dispatcher, Queue}
import cats.effect.unsafe.implicits.global
import scala.concurrent.duration._

def destroyUniverse(): Unit = { println("BOOOOM!!!"); } // stub implementation

// Synchronous effects

val s = Stream.exec(IO { destroyUniverse() }) ++ Stream("...moving on")
// s: Stream[IO[x], String] = Stream(..)
s.compile.toVector.unsafeRunSync()
// BOOOOM!!!
// res50: Vector[String] = Vector("...moving on")

val T  = Sync[IO]
// T: cats.effect.kernel.Async[IO] = cats.effect.IO$$anon$4@3cbea14d
val s2 = Stream.exec(T.delay { destroyUniverse() }) ++ Stream("...moving on")
// s2: Stream[IO[x], String] = Stream(..)
s2.compile.toVector.unsafeRunSync()
// BOOOOM!!!
// res51: Vector[String] = Vector("...moving on")

// Asynchronous effects (callbacks invoked once)

trait Connection {
  def readBytes(onSuccess: Array[Byte] => Unit, onFailure: Throwable => Unit): Unit

  // or perhaps
  def readBytesE(onComplete: Either[Throwable, Array[Byte]] => Unit): Unit =
    readBytes(bs => onComplete(Right(bs)), e => onComplete(Left(e)))

  override def toString = "<connection>"
}

val c = new Connection {
  def readBytes(onSuccess: Array[Byte] => Unit, onFailure: Throwable => Unit): Unit = {
    Thread.sleep(200)
    onSuccess(Array(0, 1, 2))
  }
}
// c: AnyRef with Connection = <connection>

val bytes = IO.async_[Array[Byte]] { cb => c.readBytesE(cb) }
// bytes: IO[Array[Byte]] = IO(...)

Stream.eval(bytes).map(_.toList).compile.toVector.unsafeRunSync()
// res52: Vector[List[Byte]] = Vector(List(0, 1, 2))

// Asynchronous effects (callbacks invoked multiple times)

type Row        = List[String]
type RowOrError = Either[Throwable, Row]

trait CSVHandle {
  def withRows(cb: RowOrError => Unit): Unit
}

def rows[F[_]](h: CSVHandle)(implicit F: Async[F]): Stream[F, Row] = {
  for {
    dispatcher <- Stream.resource(Dispatcher[F])
    q          <- Stream.eval(Queue.unbounded[F, Option[RowOrError]])
    _          <- Stream.eval {
                    F.delay {
                      def enqueue(v: Option[RowOrError]): Unit = dispatcher.unsafeRunAndForget(q.offer(v))

                      // Fill the data - withRows blocks while reading the file, asynchronously invoking the callback we pass to it on every row
                      h.withRows(e => enqueue(Some(e)))
                      // Upon returning from withRows, signal that our stream has ended.
                      enqueue(None)
                    }
                  }
    // Due to `fromQueueNoneTerminated`, the stream will terminate when it encounters a `None` value
    row        <- Stream.fromQueueNoneTerminated(q).rethrow
  } yield row
}
