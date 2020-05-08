package guide.ch18interruption

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

import cats.effect.ContextShift
import cats.effect.IO
import cats.effect.Timer
import cats.effect.concurrent.Deferred
import fs2.Stream

object App06Switcher1 extends hutil.App {

  val ec: ExecutionContext          = ExecutionContext.global
  implicit val cs: ContextShift[IO] = IO.contextShift(ec)
  implicit val timer: Timer[IO]     = IO.timer(ec)

  val program =
    Stream.eval(Deferred[IO, Unit]).flatMap { switch =>
      val switcher: Stream[IO, Unit] =
        Stream.eval(switch.complete(())).delayBy(5.seconds)

      val timeSeries: Stream[IO, Unit] =
        Stream.repeatEval(IO(println(java.time.LocalTime.now))).metered(1.second)

      timeSeries
        .interruptWhen(switch.get.attempt)
        .concurrently(switcher)
    }

  program.compile.drain.unsafeRunSync
}
