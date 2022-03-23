package guide

import fs2._
import cats.effect.{IO, IOApp}
import cats.syntax.all._
import scala.concurrent.duration._

object Ch11Interruption4 extends IOApp.Simple {

  val program: Stream[IO, Unit] =
    Stream
      .repeatEval(IO(println(java.time.LocalTime.now)))
      .metered(1.second)

  val switcher: Stream[IO, Boolean] =
    Stream[IO, Boolean](true).delayBy(5.seconds)

  val run: IO[Unit] =
    program
      .interruptWhen(switcher)
      .compile
      .drain
}
