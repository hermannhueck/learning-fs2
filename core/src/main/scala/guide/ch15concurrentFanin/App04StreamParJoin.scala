package guide.ch15concurrentFanin

import scala.concurrent.ExecutionContext
import scala.util.chaining._

import cats.effect.{ContextShift, IO}
import fs2.Stream

object App04StreamParJoin extends hutil.App {

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  val s  = Stream.range(0, 5).covary[IO]
  val s1 = s map { "A" + _.toString }
  val s2 = s map { "B" + _.toString }
  val s3 = s map { "C" + _.toString }
  val s4 = s map { "D" + _.toString }
  val s5 = s map { "E" + _.toString }

  val streamOfStreams: Stream[IO, Stream[IO, String]] =
    Stream(s1, s2, s3, s4, s5).covary[IO]

  // The parJoin function runs maxOpen streams concurrently,
  // combining their outputs nondeterministically.
  val joined =
    streamOfStreams
      .parJoin(3)
      .compile
      .toVector
      .unsafeRunSync()
      .tap(println)

  // The parJoinUnbounded function runs an unbounded number of streams concurrently,
  // combining their outputs nondeterministically.
  val joinedUnbounded =
    streamOfStreams
      .parJoinUnbounded
      .compile
      .toVector
      .unsafeRunSync()
      .tap(println)
}
