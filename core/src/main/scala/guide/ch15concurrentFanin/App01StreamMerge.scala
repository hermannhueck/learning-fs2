package guide.ch15concurrentFanin

import scala.concurrent.ExecutionContext
import scala.util.chaining._

import cats.effect.{ContextShift, IO}
import fs2.Stream
import munit.Assertions._

object App01StreamMerge extends hutil.App {

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  val s1 = Stream(1, 2, 3).covary[IO]
  val s2 = Stream.eval(IO { Thread.sleep(1000); 4 })

  // Interleaves the two inputs nondeterministically. The output stream halts after BOTH s1 and s2
  // terminate normally, or in the event of an uncaught failure on either s1 or s2.

  val merged: Stream[IO, Int] =
    s1 merge s2

  merged
    .compile
    .toVector
    .unsafeRunSync
    .tap(println)
    .pipe(assertEquals(_, Vector(1, 2, 3, 4)))
  // res0: Vector[Int] = Vector(1, 2, 3, 4)
}
