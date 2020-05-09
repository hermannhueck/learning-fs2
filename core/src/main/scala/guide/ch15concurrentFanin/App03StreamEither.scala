package guide.ch15concurrentFanin

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.chaining._

import cats.effect.{ContextShift, IO, Timer}
import fs2.Stream
import munit.Assertions._

object App03StreamEither extends hutil.App {

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  val s1 = Stream(1, 2, 3).covary[IO]
  val s2 = Stream.eval(IO { Thread.sleep(200); 4 })

  s1.either(s2)
    .compile
    .toVector
    .unsafeRunSync
    .tap(println)
    .pipe(assertEquals(_, Vector(Left(1), Left(2), Left(3), Right(4))))
  // res0: Vector[Int] = Vector(Left(1), Left(2), Left(3), Left(4))

  s2.either(s1)
    .compile
    .toVector
    .unsafeRunSync
    .tap(println)
    .pipe(assertEquals(_, Vector(Right(1), Right(2), Right(3), Left(4))))
  // res1: Vector[Int] = Vector(Right(1), Right(2), Right(3), Left(4))

  implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)

  val count = 8L

  val s3: Stream[IO, Int]              = Stream.awakeEvery[IO](1000.millis).scan(0)((acc, _) => acc + 1)
  val s4: Stream[IO, Int]              = (Stream.sleep_[IO](500.millis) ++ s3) take count
  val s5: Stream[IO, Either[Int, Int]] = s3 either s4

  val expected =
    Vector(Left(0), Right(0), Left(1), Right(1), Left(2), Right(2), Left(3), Right(3), Left(4), Right(4))
      .take(count.toInt)

  val res3: Vector[Either[Int, Int]] =
    s5.take(count)
      .compile
      .toVector
      .unsafeRunSync
      .tap(println)
      .tap(assertEquals(_, expected))
}
