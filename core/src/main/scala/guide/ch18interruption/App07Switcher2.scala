package guide.ch18interruption

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

import cats.effect.{ContextShift, IO, Timer}
import fs2.Stream

object App07Switcher2 extends hutil.App {

  val ec: ExecutionContext          = ExecutionContext.global
  implicit val cs: ContextShift[IO] = IO.contextShift(ec)
  implicit val timer: Timer[IO]     = IO.timer(ec)

  val program =
    Stream.repeatEval(IO(println(java.time.LocalTime.now))).metered(1.second).interruptAfter(5.seconds)

  program.compile.drain.unsafeRunSync()
}
