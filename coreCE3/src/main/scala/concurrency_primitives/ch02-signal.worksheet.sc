// see: https://fs2.io/#/guide - Signal

import cats.effect._
import cats.effect.unsafe.implicits.global
import fs2.Stream
import fs2.concurrent.SignallingRef

import scala.concurrent.duration._

SignallingRef[IO, Boolean](false)
  .flatMap { signal =>
    val s1 = Stream.awakeEvery[IO](1.second).interruptWhen(signal)
    val s2 = Stream.sleep[IO](4.seconds) >> Stream.eval(signal.set(true))
    s1.concurrently(s2).compile.toVector
  }
  .unsafeRunSync()
