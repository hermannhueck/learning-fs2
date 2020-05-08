package guide.ch14evalmap

import scala.util.chaining._

import cats.effect.IO
import fs2.Stream
import munit.Assertions._

object App02StreamEvalMapAccumulate extends hutil.App {

  // ----- evalMapAccumulate - Like mapAccumulate, but accepts a function returning an F[_]

  val stream: Stream[IO, String]           = Stream("Hello", "World").covary[IO]
  val accumulated: Stream[IO, (Int, Char)] = stream.evalMapAccumulate(0)((l, s) => IO(l + s.length, s.head))
  val vector: Vector[(Int, Char)]          = accumulated.compile.toVector.unsafeRunSync
  println(vector)
  assertEquals(vector, Vector((5, 'H'), (10, 'W')))

  Stream("Hello", "World")
    .covary[IO]
    .evalMapAccumulate(0)((l, s) => IO(l + s.length, s.head))
    .compile
    .toVector
    .unsafeRunSync
    .tap(println)
    .tap(assertEquals(_, Vector((5, 'H'), (10, 'W'))))

  val stream2: Stream[IO, Int] = Stream(1, 2, 3, 4).covary[IO]
  private val accumulated2     = stream2.evalMapAccumulate(0)((acc, i) => IO((i, acc + i)))
  val vector2                  = accumulated2.compile.toVector.unsafeRunSync
  println(vector2)
  assertEquals(vector2, Vector((1, 1), (2, 3), (3, 5), (4, 7)))

  Stream(1, 2, 3, 4)
    .covary[IO]
    .evalMapAccumulate(0)((acc, i) => IO((i, acc + i)))
    .compile
    .toVector
    .unsafeRunSync
    .tap(println)
    .tap(assertEquals(_, Vector((1, 1), (2, 3), (3, 5), (4, 7))))
}
