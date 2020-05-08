package guide.ch07resource

import scala.util.chaining._

import cats.effect.IO
import cats.effect.Resource
import fs2.Stream

object App01bResourceAcquisition extends hutil.App {

  val err = Stream.raiseError[IO](new Exception("oh noes!"))
  // err: fs2.Stream[cats.effect.IO,fs2.INothing] = Stream(..)

  val count = new java.util.concurrent.atomic.AtomicLong(0)
  // count: java.util.concurrent.atomic.AtomicLong = 0

  val acquire = IO { println("incremented: " + count.incrementAndGet); () }
  // acquire: cats.effect.IO[Unit] = IO$1632813798

  val release = IO { println("decremented: " + count.decrementAndGet); () }
  // release: cats.effect.IO[Unit] = IO$441860156

  val io =
    Stream
      .resource(Resource.make(acquire)(_ => release))
      .flatMap(_ => Stream(1, 2, 3) ++ err)
      .compile
      .drain

  import scala.util.Try
  Try(
    io.unsafeRunSync()
  ).fold(_.toString, _.toString) pipe println
  //=> incremented: 1
  //=> decremented: 0
  //=> java.lang.Exception: oh noes!

  // The inner stream fails, but notice the release action is still run:

  count.get pipe println
  // res27: Long = 0
}
