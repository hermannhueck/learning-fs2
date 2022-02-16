package guide.ch14evalmap

import scala.util.chaining._

import cats.effect.{ContextShift, IO}
import fs2.Stream
import hutil.stringformat._
import munit.Assertions._

object App05StreamParEvalMapUnordered extends hutil.App {

  // ----- parEvalMapUnordered: Like Stream#parEvalMap, but the order of the original stream is NOT retained.

  implicit val cs: ContextShift[IO] = IO.contextShift(scala.concurrent.ExecutionContext.Implicits.global)

  val s: Stream[IO, Int] = Stream(1, 2, 3, 4).covary[IO]

  s"$dash10 parEvalMapUnordered (resulting order of elements is NOT guaranteed)".magenta.println()
  s.parEvalMapUnordered(maxConcurrent = 2)(i => IO(println(i)))
    .compile
    .drain
    .unsafeRunSync()

  dash10.magenta.println()
  val list =
    s.parEvalMapUnordered(maxConcurrent = 2)(i => IO { println(i); i * i })
      .compile
      .toList
      .unsafeRunSync()
      .tap(println)
      .tap(l => assertEquals(l.sorted, List(1, 4, 9, 16)))

  // ----- mapAsyncUnordered is an alias for parEvalMapUnordered

  s"$dash10 mapAsyncUnordered (resulting order of elements is NOT guaranteed)".magenta.println()
  s.mapAsyncUnordered(maxConcurrent = 2)(i => IO(println(i)))
    .compile
    .drain
    .unsafeRunSync()

  dash10.magenta.println()
  val list2 =
    s.mapAsyncUnordered(maxConcurrent = 2)(i => IO { println(i); i * i })
      .compile
      .toList
      .unsafeRunSync()
      .tap(println)
      .tap(l => assertEquals(l.sorted, List(1, 4, 9, 16)))
}
