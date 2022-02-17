// see: https://fs2.io/#/guide - Building Streams

import fs2.Stream

val s0  = Stream.empty
// s0: Stream[fs2.package.Pure, Nothing] = Stream(..)
val s1  = Stream.emit(1)
// s1: Stream[Nothing, Int] = Stream(..)
val s1a = Stream(1, 2, 3)             // variadic
// s1a: Stream[Nothing, Int] = Stream(..) // variadic
val s1b = Stream.emits(List(1, 2, 3)) // accepts any Seq
// s1b: Stream[Nothing, Int] = Stream(..)

s1.toList
// res0: List[Int] = List(1)
s1.toVector
// res1: Vector[Int] = Vector(1)

(Stream(1, 2, 3) ++ Stream(4, 5)).toList
// res2: List[Int] = List(1, 2, 3, 4, 5)
Stream(1, 2, 3).map(_ + 1).toList
// res3: List[Int] = List(2, 3, 4)
Stream(1, 2, 3).filter(_ % 2 != 0).toList
// res4: List[Int] = List(1, 3)
Stream(1, 2, 3).fold(0)(_ + _).toList
// res5: List[Int] = List(6)
Stream(None, Some(2), Some(3)).collect { case Some(i) => i }.toList
// res6: List[Int] = List(2, 3)
Stream.range(0, 5).intersperse(42).toList
// res7: List[Int] = List(0, 42, 1, 42, 2, 42, 3, 42, 4)
Stream(1, 2, 3).flatMap(i => Stream(i, i)).toList
// res8: List[Int] = List(1, 1, 2, 2, 3, 3)
Stream(1, 2, 3).repeat.take(9).toList
// res9: List[Int] = List(1, 2, 3, 1, 2, 3, 1, 2, 3)
Stream(1, 2, 3).repeatN(2).toList
// res10: List[Int] = List(1, 2, 3, 1, 2, 3)

import cats.effect.IO

val eff = Stream.eval(IO { println("BEING RUN!!"); 1 + 1 })
// eff: Stream[IO, Int] = Stream(..)

// eff.toList
// error: value toList is not a member of fs2.Stream[cats.effect.IO,Int]
// eff.compile.toVector.unsafeRunSync()
// ^

import cats.effect.unsafe.implicits.global

eff.compile.toVector.unsafeRunSync()
// BEING RUN!!
// res12: Vector[Int] = Vector(2)

val ra = eff.compile.toVector       // gather all output into a Vector
// ra: IO[Vector[Int]] = IO(...) // gather all output into a Vector
val rb = eff.compile.drain          // purely for effects
// rb: IO[Unit] = IO(...) // purely for effects
val rc = eff.compile.fold(0)(_ + _) // run and accumulate some result
// rc: IO[Int] = IO(...)

ra.unsafeRunSync()
// BEING RUN!!
// res13: Vector[Int] = Vector(2)
rb.unsafeRunSync()
// BEING RUN!!
rc.unsafeRunSync()
// BEING RUN!!
// res15: Int = 2
rc.unsafeRunSync()
// BEING RUN!!
// res16: Int = 2
