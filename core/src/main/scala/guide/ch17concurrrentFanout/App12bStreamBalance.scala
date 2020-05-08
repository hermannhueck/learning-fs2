package guide.ch17concurrrentFanout

import scala.concurrent.ExecutionContext

import cats.effect.ContextShift
import cats.effect.IO
import fs2.Stream

object App12bStreamBalance extends hutil.App {

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  val balancedStream: Stream[IO, Int] =
    Stream
      .range(0, 1000)
      .covary[IO]
      .prefetchN(100)
      .balance(chunkSize = 10)
      .map(_.evalMap { o => IO { println(s">> adding 1000 to $o: ${o + 1000}"); o + 1000 } })
      .take(10)
      .parJoin(10)

  balancedStream.compile.drain.unsafeRunSync
}
