package guide.ch14evalmap

import scala.concurrent.ExecutionContext
import scala.util.chaining._

import cats.effect.{ContextShift, IO}
import fs2.Stream
import hutil.stringformat._
import munit.Assertions._

object App04StreamParEvalMap extends hutil.App {

  // ----- parEvalMap: Like Stream#evalMap, but will evaluate effects in parallel, emitting the results downstream
  // in the same order as the input stream. The number of concurrent effects is limited by the maxConcurrent parameter.
  // The order of the original stream is retained.

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  val s: Stream[IO, Int] = Stream(1, 2, 3, 4).covary[IO]

  s"$dash10 parEvalMap".magenta.println()
  s.parEvalMap(maxConcurrent = 2)(i => IO(println(i)))
    .compile
    .drain
    .unsafeRunSync()

  dash10.magenta.println()
  val list =
    s.parEvalMap(maxConcurrent = 2)(i => IO { println(i); i * i })
      .compile
      .toList
      .unsafeRunSync()
      .tap(println)
      .tap(assertEquals(_, List(1, 4, 9, 16)))

  // ----- mapAsync is an alias for parEvalMap

  s"$dash10 mapAsync".magenta.println()
  s.mapAsync(maxConcurrent = 2)(i => IO(println(i)))
    .compile
    .drain
    .unsafeRunSync()

  dash10.magenta.println()
  val list2 =
    s.mapAsync(maxConcurrent = 2)(i => IO { println(i); i * i })
      .compile
      .toList
      .unsafeRunSync()
      .tap(println)
      .tap(assertEquals(_, List(1, 4, 9, 16)))
}
