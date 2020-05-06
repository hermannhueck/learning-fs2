package guide.ch14evalmap

import scala.util.chaining._
import munit.Assertions._
import fs2.{Pure, Stream}

object App01StreamMapAccumulate extends hutil.App {

  // ----- mapAccumulate maps a running total according to initial value and the input with the function f

  val stream: Stream[Pure, String]           = Stream("Hello", "World")
  val accumulated: Stream[Pure, (Int, Char)] = stream.mapAccumulate(0)((l, s) => (l + s.length, s.head))
  val vector: Vector[(Int, Char)]            = accumulated.toVector
  println(vector)
  assertEquals(vector, Vector((5, 'H'), (10, 'W')))

  Stream("Hello", "World")
    .mapAccumulate(0)((l, s) => (l + s.length, s.head))
    .toVector
    .tap(println)
    .tap(assertEquals(_, Vector((5, 'H'), (10, 'W'))))
}
