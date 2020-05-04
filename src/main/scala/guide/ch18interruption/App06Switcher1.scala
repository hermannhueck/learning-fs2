package guide.ch18interruption

import cats.effect.concurrent.Deferred
import cats.effect.{ContextShift, IO, Timer}
import cats.syntax.flatMap._
import fs2.Stream
import cats.syntax.either._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

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
