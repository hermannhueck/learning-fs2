package guide.ch18interruption

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

import cats.effect.ContextShift
import cats.effect.IO
import cats.effect.Timer
import fs2.Stream

object App01StreamInterruptAfter extends hutil.App {

  val ec: ExecutionContext          = ExecutionContext.global
  implicit val cs: ContextShift[IO] = IO.contextShift(ec)
  implicit val timer: Timer[IO]     = IO.timer(ec)

  val stream: Stream[IO, Int] =
    Stream
      .range(1, 100)
      .zipLeft(Stream.awakeEvery[IO](250.milliseconds))
      .evalTap(i => IO(println(i)))

  stream
    .interruptAfter(2600.milliseconds)
    .compile
    .drain
    .unsafeRunSync
}
