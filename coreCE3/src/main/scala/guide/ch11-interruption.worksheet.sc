// see: https://fs2.io/#/guide - Interruption

import fs2._
import cats.effect.{Deferred, IO}
import cats.effect.unsafe.implicits.global
import scala.concurrent.duration._

val program =
  Stream.eval(Deferred[IO, Unit]).flatMap { switch =>
    val switcher =
      Stream.eval(switch.complete(())).delayBy(5.seconds)

    val program =
      Stream.repeatEval(IO(println(java.time.LocalTime.now))).metered(1.second)

    program
      .interruptWhen(switch.get.attempt)
      .concurrently(switcher)
  }
// program: Stream[IO[x], Unit] = Stream(..)

program.compile.drain.unsafeRunSync()
// 13:25:19.150544539
// 13:25:20.149489803
// 13:25:21.148694470
// 13:25:22.148740143

val program1 =
  Stream.repeatEval(IO(println(java.time.LocalTime.now))).metered(1.second).interruptAfter(5.seconds)
// program1: Stream[IO[x], Unit] = Stream(..)

program1.compile.drain.unsafeRunSync()
// 13:25:24.154852237
// 13:25:25.154609036
// 13:25:26.154660866
// 13:25:27.154799995
