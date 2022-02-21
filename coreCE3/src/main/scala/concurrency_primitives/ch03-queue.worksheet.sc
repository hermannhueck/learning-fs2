// see: https://fs2.io/#/guide - Queue

import cats.effect.IO
import cats.effect.std.Queue
import cats.effect.unsafe.implicits.global
import cats.syntax.all._
import fs2.Stream

val program = for {
  queue          <- Queue.unbounded[IO, Option[Int]]
  streamFromQueue = Stream.fromQueueNoneTerminated(queue) // Stream is terminated when None is returned from queue
  _              <- Seq(Some(1), Some(2), Some(3), None).map(queue.offer).sequence
  result         <- streamFromQueue.compile.toList
} yield result

program.unsafeRunSync()
