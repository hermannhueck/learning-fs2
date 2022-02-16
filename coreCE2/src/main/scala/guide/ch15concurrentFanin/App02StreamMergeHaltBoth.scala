package guide.ch15concurrentFanin

import scala.concurrent.ExecutionContext
import scala.util.chaining._

import cats.effect.{ContextShift, IO}
import fs2.Stream
import munit.Assertions._

object App02StreamMergeHaltBoth extends hutil.App {

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  val s1 = Stream(1, 2, 3).covary[IO]
  val s2 = Stream.eval(IO { Thread.sleep(200); 4 })

  // Like merge, but halts as soon as _either_ branch halts.

  val merged: Stream[IO, Int] =
    s1 mergeHaltBoth s2

  // ---- see also mergeHaltL and mergeHaltR

  merged
    .compile
    .toVector
    .unsafeRunSync()
    .tap(println)
    .pipe(assertEquals(_, Vector(1, 2, 3)))
  // res0: Vector[Int] = Vector(1, 2, 3)
}
