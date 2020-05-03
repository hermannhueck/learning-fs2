package guide.ch14evalmap

import scala.util.chaining._
import hutil.stringformat._
import munit.Assertions._
import cats.effect.IO
import fs2.{Pure, Stream}

object App03StreamEvalMap extends hutil.App {

  // ----- evalMap is an alias for flatMap(o => Stream.eval(f(o)))

  val s: Stream[IO, Int] = Stream(1, 2, 3, 4).covary[IO]

  s.flatMap(i => Stream.eval(IO(println(i)))).compile.drain.unsafeRunSync

  dash10.magenta.println
  s.evalMap(i => IO(println(i))).compile.drain.unsafeRunSync

  dash10.magenta.println
  val list1 =
    s.flatMap(i => Stream.eval(IO { println(i); i * i }))
      .compile
      .toList
      .unsafeRunSync
      .tap(println)
      .tap(assertEquals(_, List(1, 4, 9, 16)))

  dash10.magenta.println
  val list2 =
    s.evalMap(i => IO { println(i); i * i })
      .compile
      .toList
      .unsafeRunSync
      .tap(println)
      .tap(assertEquals(_, List(1, 4, 9, 16)))
}
