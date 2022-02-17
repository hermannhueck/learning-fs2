// see: https://fs2.io/#/guide - Basic Stream Operations

import fs2.Stream
import cats.effect.IO
import cats.effect.unsafe.implicits.global

val appendEx1 = Stream(1, 2, 3) ++ Stream.emit(42)
// appendEx1: Stream[Nothing, Int] = Stream(..)
val appendEx2 = Stream(1, 2, 3) ++ Stream.eval(IO.pure(4))
// appendEx2: Stream[IO[A], Int] = Stream(..)

appendEx1.toVector
// res17: Vector[Int] = Vector(1, 2, 3, 42)
appendEx2.compile.toVector.unsafeRunSync()
// res18: Vector[Int] = Vector(1, 2, 3, 4)

appendEx1.map(_ + 1).toList
// res19: List[Int] = List(2, 3, 4, 43)

appendEx1.flatMap(i => Stream.emits(List(i, i))).toList
// res20: List[Int] = List(1, 1, 2, 2, 3, 3, 42, 42)
