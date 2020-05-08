package guide.ch09exercise1

import scala.util.chaining._

import cats.effect.IO
import fs2.INothing
import fs2.Stream
import munit.Assertions._

object App01Exercises extends hutil.App {

  implicit class exercises[+F[_], +O](s: Stream[F, O]) {
    def repeatIt(): Stream[F, O]                     = s ++ s.repeatIt()
    def drainIt(): Stream[F, INothing]               = s >> Stream.empty
    def attemptIt(): Stream[F, Either[Throwable, O]] = s.map(o => Right(o)).handleErrorWith(t => Stream(Left(t)))
  }

  def exEval_[F[_], O](fa: F[O]): Stream[F, INothing] = Stream.eval(fa) >> Stream.empty

  println("\n>>> repeat:")
  val repeated: List[Int] = Stream(1, 0).repeat.take(6).toList
  println(repeated)
  val exRepeated: List[Int] = Stream(1, 0).repeatIt().take(6).toList
  println(exRepeated)
  assertEquals(repeated, exRepeated)

  println("\n>>> drain:")
  val drained: List[INothing] = Stream(1, 2, 3).drain.toList
  println(drained)
  val exDrained: List[INothing] = Stream(1, 2, 3).drainIt().toList
  println(exDrained)
  assertEquals(exDrained, drained)

  println("\n>>> eval_:")
  val evaluated: Vector[INothing] = Stream.eval_(IO(println("!!"))).compile.toVector.unsafeRunSync()
  println(evaluated)
  val exEvaluated: Vector[INothing] = exEval_(IO(println("!!"))).compile.toVector.unsafeRunSync()
  println(exEvaluated)
  assertEquals(exEvaluated, evaluated)

  println("\n>>> attempt:")
  val errStream: Stream[IO, Int] =
    Stream(1, 2).covary[IO] ++ Stream.raiseError[IO](new Exception("nooo!!!"))

  val attempted: List[Either[Throwable, Int]] =
    errStream.attempt.compile.toList.unsafeRunSync tap println

  val exAttempted: List[Either[Throwable, Int]] =
    errStream.attemptIt.compile.toList.unsafeRunSync tap println

  assertEquals(exAttempted, attempted)
}
