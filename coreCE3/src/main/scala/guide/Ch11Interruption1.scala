package guide

import fs2._
import cats.effect.{IO, IOApp}
import scala.concurrent.duration._

object Ch11Interruption1 extends IOApp.Simple {

  val program: Stream[IO, Unit] =
    Stream
      .repeatEval(IO(println(java.time.LocalTime.now)))
      .metered(1.second)

  val run: IO[Unit] =
    program
      .interruptAfter(5.seconds)
      .compile
      .drain
}
