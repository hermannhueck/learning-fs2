package guide

import fs2._
import fs2.concurrent._
import cats.effect.{IO, IOApp}
import scala.concurrent.duration._

object Ch11Interruption2 extends IOApp.Simple {

  val program: Stream[IO, Unit] =
    Stream
      .repeatEval(IO(println(java.time.LocalTime.now)))
      .metered(1.second)

  def switcher(signal: SignallingRef[IO, Boolean]) =
    Stream.eval(signal.set(true)).delayBy(5.seconds)

  val run: IO[Unit] =
    Stream
      .eval(SignallingRef[IO, Boolean](false))
      .flatMap { signal =>
        program
          .interruptWhen(signal)
          .concurrently(switcher(signal))
      }
      .compile
      .drain
}
