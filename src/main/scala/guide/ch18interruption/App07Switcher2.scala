package guide.ch18interruption

import cats.effect.concurrent.Deferred
import cats.effect.{ContextShift, IO, Timer}
import cats.syntax.flatMap._
import fs2.Stream
import cats.syntax.either._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object App07Switcher2 extends hutil.App {

  val ec: ExecutionContext          = ExecutionContext.global
  implicit val cs: ContextShift[IO] = IO.contextShift(ec)
  implicit val timer: Timer[IO]     = IO.timer(ec)

  val program =
    Stream.repeatEval(IO(println(java.time.LocalTime.now))).metered(1.second).interruptAfter(5.seconds)

  program.compile.drain.unsafeRunSync
}
