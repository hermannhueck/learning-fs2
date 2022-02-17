// see: https://fs2.io/#/guide - Error Handling

import fs2.Stream
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import munit.Assertions._

implicit class exercises[+F[_], +O](s: Stream[F, O]) {
  def repeatIt(): Stream[F, O]                     = s ++ s.repeatIt()
  def drainIt(): Stream[F, Nothing]                = s >> Stream.empty
  def attemptIt(): Stream[F, Either[Throwable, O]] = s.map(o => Right(o)).handleErrorWith(t => Stream(Left(t)))
}

println(">>> repeat:")
val repeated: List[Int]   = Stream(1, 0).repeat.take(6).toList
println(repeated)
val exRepeated: List[Int] = Stream(1, 0).repeatIt().take(6).toList
println(exRepeated)
assertEquals(repeated, exRepeated)

println(">>> drain:")
val drained: List[Nothing]   = Stream(1, 2, 3).drain.toList
println(drained)
val exDrained: List[Nothing] = Stream(1, 2, 3).drainIt().toList
println(exDrained)
assertEquals(exDrained, drained)

println(">>> attempt:")
val errStream: Stream[IO, Int] =
  Stream(1, 2).covary[IO] ++ Stream.raiseError[IO](new Exception("nooo!!!"))

val attempted: List[Either[Throwable, Int]] =
  errStream
    .attempt
    .compile
    .toList
    .unsafeRunSync()

val exAttempted: List[Either[Throwable, Int]] =
  errStream
    .attemptIt()
    .compile
    .toList
    .unsafeRunSync()

assertEquals(exAttempted.length, attempted.length)
assertEquals(exAttempted(0), attempted(0))
assertEquals(exAttempted(1), attempted(1))
exAttempted(2)
attempted(2)
assertEquals(exAttempted(2).toString(), attempted(2).toString())

import fs2.INothing

def exExec[F[_]](action: F[Unit]): Stream[F, INothing] = Stream.eval(action) >> Stream.empty

println(">>> exec:")
val evaluated: Vector[Nothing]   = Stream.exec(IO(println("!!"))).compile.toVector.unsafeRunSync()
println(evaluated)
val exEvaluated: Vector[Nothing] = exExec(IO(println("!!"))).compile.toVector.unsafeRunSync()
println(exEvaluated)
assertEquals(exEvaluated, evaluated)
