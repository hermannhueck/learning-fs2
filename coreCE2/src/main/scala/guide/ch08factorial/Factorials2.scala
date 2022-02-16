package guide.ch08factorial

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

import cats.effect.{IO, Timer}
import fs2.Stream

object Factorials2 extends hutil.App {

  implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)

  val ints: Stream[IO, Int]          = Stream.range(1, 31).covary[IO]
  val factorials: Stream[IO, BigInt] =
    ints.scan(BigInt(1))((acc, next) => acc * next)

  val stream: Stream[IO, Unit] =
    factorials
      .zipWithIndex
      .map { case num -> index => s"$index = $num" }
      .zipLeft(Stream.fixedRate[IO](250.millis))
      .lines(java.lang.System.out)

  stream.compile.drain.unsafeRunSync()
}
