package guide.ch20reactivestreams

import scala.concurrent.ExecutionContext

import cats.effect.{ContextShift, IO}
import fs2.Stream
import fs2.interop.reactivestreams._

object App01ReactiveStreams extends hutil.App {

  implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  val stream: Stream[IO, Int] = Stream(1, 2, 3).covary[IO]
  // stream: fs2.Stream[cats.effect.IO,Int] = Stream(..)

  val publisher: StreamUnicastPublisher[IO, Int] = stream.toUnicastPublisher

  val stream2: Stream[IO, Int] = publisher.toStream[IO]

  stream2.compile.toList.map(println).unsafeRunSync()
}
