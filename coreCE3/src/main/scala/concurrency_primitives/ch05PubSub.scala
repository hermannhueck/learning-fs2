// see: https://fs2.io/#/guide - single Publisher, Multiple Subscribers

package concurrency_primitives

import scala.concurrent.duration._
import cats.effect.{Clock, IO, IOApp, Temporal}
import cats.effect.std.Console
import cats.syntax.all._
import fs2.{INothing, Pipe, Stream}
import fs2.concurrent.{SignallingRef, Topic}

sealed trait Event
case class Text(value: String) extends Event
case object Quit               extends Event

class EventService[F[_]](eventsTopic: Topic[F, Event], interrupter: SignallingRef[F, Boolean])(implicit
    F: Temporal[F],
    console: Console[F]
) {

  // Publishing 15 text events, then single Quit event, and still publishing text events
  def startPublisher: Stream[F, Unit] = {
    val textEvents =
      Stream
        .awakeEvery[F](1.second)
        .zipRight(Stream.repeatEval(Clock[F].realTime.map(t => Text(t.toString))))

    val quitEvent = Stream.eval(eventsTopic.publish1(Quit).as(Quit))

    (textEvents.take(15) ++ quitEvent ++ textEvents)
      .through(eventsTopic.publish)
      .interruptWhen(interrupter)
  }

  // Creating 3 subscribers in a different period of time and join them to run concurrently
  def startSubscribers: Stream[F, Unit] = {
    def processEvent(subscriberNumber: Int): Pipe[F, Event, INothing] =
      _.foreach {
        case e @ Text(_) =>
          console.println(s"Subscriber #$subscriberNumber processing event: $e")
        case Quit        =>
          console.println(s"Subscriber #$subscriberNumber processing event: QUIT")
          interrupter.set(true)
      }

    val events: Stream[F, Event] =
      eventsTopic.subscribe(10)

    Stream(
      events.through(processEvent(1)),
      events.delayBy(5.second).through(processEvent(2)),
      events.delayBy(10.second).through(processEvent(3))
    ).parJoin(3)
  }
}

object PubSub extends IOApp.Simple {

  val program = for {
    topic  <- Stream.eval(Topic[IO, Event])
    signal <- Stream.eval(SignallingRef[IO, Boolean](false))
    service = new EventService[IO](topic, signal)
    _      <- service.startPublisher.concurrently(service.startSubscribers)
  } yield ()

  def run: IO[Unit] = program.compile.drain
}
