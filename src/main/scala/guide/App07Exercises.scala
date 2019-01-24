package guide

import cats.effect.IO
import fs2.{INothing, Stream}

import scala.language.higherKinds

object App07Exercises extends App {

  println("\n-----")

  implicit class exercises[+F[_], +O](s: Stream[F, O]) {
    def repeatIt(): Stream[F, O] = s ++ s.repeatIt()
    def drainIt(): Stream[F, INothing] = s >> Stream.empty
    def attemptIt(): Stream[F, Either[Throwable, O]] = s.map(o => Right(o)).handleErrorWith(t => Stream(Left(t)))
  }

  def exEval_[F[_], O](fa: F[O]): Stream[F, INothing] = Stream.eval(fa) >> Stream.empty

  println("\nrepeat:")
  val repeated: List[Int] = Stream(1,0).repeat.take(6).toList
  println(repeated)
  val exRepeated: List[Int] = Stream(1,0).repeatIt().take(6).toList
  println(exRepeated)

  println("\ndrain:")
  val drained: List[INothing] = Stream(1,2,3).drain.toList
  println(drained)
  val exDrained: List[INothing] = Stream(1,2,3).drainIt().toList
  println(exDrained)

  println("\neval_:")
  val evaluated_ : Vector[INothing] = Stream.eval_(IO(println("!!"))).compile.toVector.unsafeRunSync()
  println(evaluated_)
  val exEvaluated_ : Vector[INothing] = exEval_(IO(println("!!"))).compile.toVector.unsafeRunSync()
  println(exEvaluated_)

  println("\nattempt:")
  val attempted: List[Either[Throwable, Int]] = (Stream(1,2) ++ (throw new Exception("nooo!!!"))).attempt.toList
  println(attempted)
  val exAttempted: List[Either[Throwable, Int]] = (Stream(1,2) ++ (throw new Exception("nooo!!!"))).attemptIt().toList
  println(exAttempted)

  println("-----\n")
}
