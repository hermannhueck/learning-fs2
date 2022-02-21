// see: https://fs2.io/#/guide - Appendixes

import fs2._
import cats.effect.IO
import cats.effect.unsafe.implicits.global

case object Err extends Throwable

(Stream(1) ++ Stream(2)
  .map(_ => throw Err))
  .take(1)
  .toList
// res58: List[Int] = List(1)
(Stream(1) ++ Stream
  .raiseError[IO](Err))
  .take(1)
  .compile
  .toList
  .unsafeRunSync()
// res59: List[Int] = List(1)

Stream(1)
  .covary[IO]
  .onFinalize(IO { println("finalized!") })
  .take(1)
  .compile
  .toVector
  .unsafeRunSync()
// finalized!
// res61: Vector[Int] = Vector(1)

val s1     = (Stream(1) ++ Stream(2)).covary[IO]
// s1: Stream[IO, Int] = Stream(..)
val s2     = (Stream.empty ++ Stream.raiseError[IO](Err)).handleErrorWith { e => println(e); Stream.raiseError[IO](e) }
// s2: Stream[IO[x], Nothing] = Stream(..)
val merged = s1 merge s2 take 1
// merged: Stream[IO[x], Int] = Stream(..)

/*
The result is highly nondeterministic. Here are a few ways it can play out:

s1 may complete before the error in s2 is encountered, in which case nothing will be printed and no error will occur.
s2 may encounter the error before any of s1 is emitted. When the error is reraised by s2, that will terminate the merge and asynchronously interrupt s1, and the take terminates with that same error.
s2 may encounter the error before any of s1 is emitted, but during the period where the value is caught by handleErrorWith, s1 may emit a value and the take(1) may terminate, triggering interruption of both s1 and s2, before the error is reraised but after the exception is printed! In this case, the stream will still terminate without error.
The correctness of your program should not depend on how different streams interleave, and once again, you should not use handleErrorWith or other interruptible functions for resource cleanup. Use bracket or onFinalize for this purpose.
 */
