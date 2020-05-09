package guide.ch18interruption

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

import cats.effect.{ContextShift, IO, Timer}
import cats.syntax.either._
import cats.syntax.flatMap._
import fs2.Stream

object App02StreamInterruptWhen extends hutil.App {

  val ec: ExecutionContext          = ExecutionContext.global
  implicit val cs: ContextShift[IO] = IO.contextShift(ec)
  implicit val timer: Timer[IO]     = IO.timer(ec)

  val stream: Stream[IO, Int] =
    Stream
      .range(1, 100)
      .zipLeft(Stream.awakeEvery[IO](250.milliseconds))
      .evalTap(i => IO(println(i)))

  val interruptOnTermination: IO[Either[Throwable, Unit]] =
    IO.sleep(2600.milliseconds) >> IO(println("TIMEOUT")) >> IO { (new RuntimeException).asLeft[Unit] }
  // IO.sleep(2600.milliseconds) >> IO(println("TIMEOUT")) >> IO(().asRight[Throwable])

  val interruptedStream: Stream[IO, Int] =
    stream
      .interruptWhen(interruptOnTermination)
      .handleErrorWith { _ /*throwable*/ => Stream(-1000) } // prevents the exeption being thrown

  interruptedStream.compile.drain.unsafeRunSync
}
