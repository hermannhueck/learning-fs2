package guide

import fs2._
import cats.effect.{IO, IOApp}
import cats.effect.kernel.Deferred
import scala.concurrent.duration._

object Ch11Interruption0 extends IOApp.Simple {

  val program: Stream[IO, Unit] =
    Stream
      .repeatEval(IO(println(java.time.LocalTime.now)))
      .metered(1.second)

  def switcher(switch: Deferred[IO, Unit]) =
    Stream.eval(switch.complete(())).delayBy(5.seconds)

  val run: IO[Unit] =
    Stream
      .eval(Deferred[IO, Unit])
      .flatMap { switch =>
        program
          .interruptWhen(switch.get.attempt)
          .concurrently(switcher(switch))
      }
      .compile
      .drain
}
