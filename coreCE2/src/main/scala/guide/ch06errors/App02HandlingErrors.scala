package guide.ch06errors

import scala.util.chaining._

import cats.effect.IO
import cats.syntax.applicativeError._
import fs2.Stream

object App02HandlingErrors extends hutil.App {

  val errStream: Stream[IO, Int] =
    Stream(0, 1, 2, 3) ++ Stream.raiseError[IO](new IllegalStateException("!@#$"))

  // The handleErrorWith method lets us catch any of these errors.
  // It turns the error into a Stream taking a function: Throwable => Stream[F, O]
  //
  println("\n>>> errStream.handleErrorWith")
  errStream
    .handleErrorWith {
      case _: IllegalStateException => Stream.emit(-1).covary[IO]
      case t: Throwable             => Stream.raiseError[IO](t)
    }
    .compile
    .toList
    .unsafeRunSync() pipe println

  // The handleError method (from ApplicativeError syntax) lets us also catch errors:
  // It turns the error into a pure value taking a function: Throwable => O
  //
  println("\n>>> errStream.handleError")
  errStream
    .handleError {
      case _: IllegalStateException => -1
      case t: Throwable             => throw t // scalafix:ok DisableSyntax.throw
    }
    .compile
    .toList
    .unsafeRunSync() pipe println

  // The recoverWith method (from ApplicativeError syntax) corresponds to handleErrorWith:
  // It turns the error into a Stream taking a partial function: PartialFunction[Throwable, Stream[F, O]]
  //
  println("\n>>> errStream.recoverWith")
  errStream
    .recoverWith { case _: IllegalStateException =>
      Stream.emit(-1)
    }
    .compile
    .toList
    .unsafeRunSync() pipe println

  // The recover method (from ApplicativeError syntax) corresponds to handleError:
  // It turns the error into a pure value taking a partial function: PartialFunction[Throwable, O]
  //
  println("\n>>> errStream.recover")
  errStream
    .recover { case _: IllegalStateException =>
      -1
    }
    .compile
    .toList
    .unsafeRunSync() pipe println

  // The attempt method (from ApplicativeError syntax) turns the Stream elements into an Either[Throwable, Int]
  //
  println("\n>>> errStream.attempt")
  val eitherStream: Stream[IO, Either[Throwable, Int]] = errStream.attempt
  eitherStream.compile.toList.unsafeRunSync() pipe println

  // The ensure method (from MonadError syntax) that the given predicate holds for all elements of the stream
  import cats.syntax.monadError._
  //
  println("\n>>> errStream.ensure")
  val ensured: Stream[IO, Int] = errStream.ensure(new IllegalStateException("odd value"))(_ % 2 != 0)
  ensured.attempt.compile.toList.unsafeRunSync() pipe println
}
