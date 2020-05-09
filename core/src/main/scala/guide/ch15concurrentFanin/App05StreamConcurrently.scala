package guide.ch15concurrentFanin

import scala.concurrent.ExecutionContext
import scala.util.chaining._

import cats.effect.{ContextShift, IO}
import fs2.Stream
import fs2.concurrent.SignallingRef
import munit.Assertions._

object App05StreamConcurrently extends hutil.App {

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  val data: Stream[IO, Int] =
    Stream.range(1, 10).covary[IO]

  val signal: IO[SignallingRef[IO, Int]] =
    fs2.concurrent.SignallingRef[IO, Int](0)

  val intStream: Stream[IO, Int] = Stream
    .eval(signal)
    .flatMap { sigRef =>
      Stream(sigRef) concurrently data.evalMap(value => sigRef.set(value)) // concurrent streams
    }
    .flatMap { sig =>
      sig.discrete // stream of the updates to this signal
    }
    .takeWhile(_ < 9, takeFailure = true)

  val result: Option[Int] = intStream
    .compile
    .last
    .unsafeRunSync
    .tap(println)
    .tap(assertEquals(_, Some(9)))
}
