package guide.ch21howstreaminterruptionworks

import scala.util.chaining._

import cats.effect.IO
import fs2.Stream

object App01SyncInterrupts extends hutil.App {

  case object Err extends Throwable

  val res01: List[Int] =
    (Stream(1) ++ Stream(2).map(_ => throw Err))
      .take(1)
      .toList
      .tap(println)

  // The take 1 uses Pull but doesn’t examine the entire stream, and neither of these examples will ever throw an error.
  // This makes sense. A bit more subtle is that this code will also never throw an error:

  val res02: List[Int] =
    (Stream(1) ++ Stream.raiseError[IO](Err))
      .take(1)
      .compile
      .toList
      .unsafeRunSync
      .tap(println)

  // The reason is simple: the consumer (the take(1)) terminates as soon as it has an element. Once it has that element,
  // it is done consuming the stream and doesn’t bother running any further steps of it,
  // so the stream never actually completes normally—it has been interrupted before that can occur.
  // We may be able to see in this case that nothing follows the emitted 1,
  // but FS2 doesn’t know this until it actually runs another step of the stream.
  //
  // If instead we use onFinalize, the code is guaranteed to run, regardless of whether take interrupts:

  val res03: Vector[Int] =
    Stream(1)
      .covary[IO]
      .onFinalize(IO { println("\n>>> finalized!") })
      .take(1)
      .compile
      .toVector
      .unsafeRunSync
      .tap(println)
  // finalized!
  // res03: Vector[Int] = Vector(1)
}
