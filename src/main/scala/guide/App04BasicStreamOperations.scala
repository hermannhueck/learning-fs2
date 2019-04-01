package guide

import cats.effect.IO
import fs2.{Pure, Stream}

object App04BasicStreamOperations extends App {

  println("\n-----")

  // Streams have a small but powerful set of operations, some of which we’ve seen already.
  // The key operations are ++, map, flatMap, handleErrorWith, and bracket:

  val appendEx1: Stream[Pure, Int] = Stream(1,2,3) ++ Stream.emit(42)
  // appendEx1: fs2.Stream[[x]fs2.Pure[x],Int] = Stream(..)

  val appendEx2: Stream[IO, Int] = Stream(1,2,3) ++ Stream.eval(IO.pure(4))
  // appendEx2: fs2.Stream[cats.effect.IO,Int] = Stream(..)

  val res0 = appendEx1.toVector
  // res0: Vector[Int] = Vector(1, 2, 3, 42)

  val res1 = appendEx2.compile.toVector.unsafeRunSync()
  // res1: Vector[Int] = Vector(1, 2, 3, 4)

  val res2 = appendEx1.map(_ + 1).toList
  // res2: List[Int] = List(2, 3, 4, 43)

  // The flatMap operation is the same idea as in lists - it maps, then concatenates:

  val res3 = appendEx1.flatMap(i => Stream.emits(List(i,i))).toList
  // res3: List[Int] = List(1, 1, 2, 2, 3, 3, 42, 42)

  println("-----\n")
}
