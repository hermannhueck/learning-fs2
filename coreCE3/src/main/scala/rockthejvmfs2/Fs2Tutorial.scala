// See:
// - https://blog.rockthejvm.com/fs2/
// - https://www.youtube.com/watch?v=XCpGtaJjkVY&list=PLmtsMNDRU0ByzHzqLdoaeuKntdwCCB1d3&index=8

package rockthejvmfs2

object Model {
  case class Actor(id: Int, firstName: String, lastName: String)
}

object Data {
  import Model._
  // Justice League
  val henryCavil: Actor = Actor(0, "Henry", "Cavill")
  val galGodot: Actor   = Actor(1, "Gal", "Godot")
  val ezraMiller: Actor = Actor(2, "Ezra", "Miller")
  val benFisher: Actor  = Actor(3, "Ben", "Fisher")
  val rayHardy: Actor   = Actor(4, "Ray", "Hardy")
  val jasonMomoa: Actor = Actor(5, "Jason", "Momoa")

  // Avengers
  val scarlettJohansson: Actor = Actor(6, "Scarlett", "Johansson")
  val robertDowneyJr: Actor    = Actor(7, "Robert", "Downey Jr.")
  val chrisEvans: Actor        = Actor(8, "Chris", "Evans")
  val markRuffalo: Actor       = Actor(9, "Mark", "Ruffalo")
  val chrisHemsworth: Actor    = Actor(10, "Chris", "Hemsworth")
  val jeremyRenner: Actor      = Actor(11, "Jeremy", "Renner")
  val tomHolland: Actor        = Actor(13, "Tom", "Holland")
  val tobeyMaguire: Actor      = Actor(14, "Tobey", "Maguire")
  val andrewGarfield: Actor    = Actor(15, "Andrew", "Garfield")
}

import cats.effect.std.Queue
import cats.effect.{IO, IOApp}
import cats.syntax.all._
import fs2.{Chunk, Pipe, Pull, Pure, Stream}
import scala.concurrent.duration._

object Fs2Tutorial extends IOApp.Simple {
  import Model._
  import Data._

  // 2. Building a Stream

  val jlActors: Stream[Pure, Actor] = Stream(
    henryCavil,
    galGodot,
    ezraMiller,
    benFisher,
    rayHardy,
    jasonMomoa
  )

  val tomHollandStream: Stream[Pure, Actor] = Stream.emit(tomHolland)
  val spiderMen: Stream[Pure, Actor]        = Stream.emits(
    List(
      tomHolland,
      tobeyMaguire,
      andrewGarfield
    )
  )

  val jlActorList: List[Actor]     = jlActors.toList
  val jlActorVector: Vector[Actor] = jlActors.toVector

  val infiniteJLActors: Stream[Pure, Actor] = jlActors.repeat
  val repeatedJLActorsList: List[Actor]     = infiniteJLActors.take(12).toList

  val liftedJlActors: Stream[IO, Actor] = jlActors.covary[IO]

  @annotation.nowarn("cat=unused-params")
  def jlActorStream[F[_]: cats.MonadThrow]: Stream[F, Actor] = jlActors.covary[F]

  val savingTomHolland: Stream[IO, Unit] = Stream.eval {
    IO {
      println(s"Saving actor $tomHolland")
      Thread.sleep(1000)
      println("Finished")
    }
  }

  val compiledStream: IO[Unit] = savingTomHolland.compile.drain

  val jlActorsEffectfulList: IO[List[Actor]] = liftedJlActors.compile.toList

  // override def run: IO[Unit] = for {
  //   _ <- IO.println("------------------------------------------------------")
  //   _ <- savingTomHolland.compile.drain.void
  //   _ <- IO.println("------------------------------------------------------")
  // } yield ()

  // 2.1. Chunks

  val avengersActors: Stream[Pure, Actor] = Stream.chunk(
    Chunk.array(
      Array(
        scarlettJohansson,
        robertDowneyJr,
        chrisEvans,
        markRuffalo,
        chrisHemsworth,
        jeremyRenner
      )
    )
  )

  // 3. Transforming a Stream

  // Stream#++
  val dcAndMarvelSuperheroes: Stream[Pure, Actor] = jlActors ++ avengersActors

  // Stream#flatMap
  val printedJlActors: Stream[IO, Unit] = jlActors.flatMap { actor =>
    Stream.eval(IO.println(actor))
  }

  // Stream#evalMap
  val evalMappedJlActors: Stream[IO, Unit] = jlActors.evalMap(IO.println)

  // Stream#evalTap
  val evalTappedJlActors: Stream[IO, Actor] = jlActors.evalTap(IO.println)

  // Stream#fold
  val avengersActorsByFirstName: Stream[Pure, Map[String, List[Actor]]] =
    avengersActors.fold(Map.empty[String, List[Actor]]) { (map, actor) =>
      map + (actor.firstName -> (actor :: map.getOrElse(actor.firstName, Nil)))
    }

  val fromActorToStringPipe: Pipe[IO, Actor, String] = in => in.map(actor => s"${actor.firstName} ${actor.lastName}")
  def toConsole[T]: Pipe[IO, T, Unit]                = in => in.evalMap(str => IO.println(str))
  val stringNamesOfJlActors: Stream[IO, Unit]        =
    jlActors.through(fromActorToStringPipe).through(toConsole)

  // 4. Error Handling in Streams

  object ActorRepository {

    def save(actor: Actor): IO[Int] = IO {
      println(s"Saving actor: $actor")
      if (scala.util.Random.nextInt() % 2 == 0) {
        throw new RuntimeException("Something went wrong during the communication with the persistence layer")
      }
      println(s"Saved.")
      actor.id
    }
  }

  val savedJlActors: Stream[IO, Int] = jlActors.evalMap(ActorRepository.save)

  val errorHandledSavedJlActors: Stream[IO, AnyVal] =
    savedJlActors.handleErrorWith(error => Stream.eval(IO.println(s"Error: $error")))

  val attemptedSavedJlActors: Stream[IO, Either[Throwable, Int]] = savedJlActors.attempt
  val printedSavedJlActors                                       = attemptedSavedJlActors.evalMap {
    case Left(error) => IO.println(s"Error: $error")
    case Right(id)   => IO.println(s"Saved actor with id: $id")
  }

  // 5. Resource Management

  case class DatabaseConnection(connection: String) extends AnyVal

  val acquire = IO {
    val conn = DatabaseConnection("jlaConnection")
    println(s"--> Acquiring connection to the database: $conn")
    conn
  }

  val release = (conn: DatabaseConnection) => IO.println(s"<-- Releasing connection to the database: $conn")

  val managedJlActors: Stream[IO, Int] =
    Stream.bracket(acquire)(release).flatMap(_ => savedJlActors)

  // 6. The Pull Type

  val tomHollandActorPull: Pull[Pure, Actor, Unit] = Pull.output1(tomHolland)

  val tomHollandActorStream: Stream[Pure, Actor] = tomHollandActorPull.stream

  val spiderMenActorPull: Pull[Pure, Actor, Unit] =
    tomHollandActorPull >> Pull.output1(tobeyMaguire) >> Pull.output1(andrewGarfield)

  val avengersActorsPull: Pull[Pure, Actor, Unit] = avengersActors.pull.echo

  // fs2 library code
  // final class ToPull[F[_], O] private[Stream] (private val self: Stream[F, O]) extends AnyVal
  // fs2 library code
  // def echo: Pull[F, O, Unit] = self.underlying
  // fs2 library code
  // final class Stream[+F[_], +O] private[fs2] (private[fs2] val underlying: Pull[F, O, Unit])

  val unconsAvengersActors: Pull[Pure, Nothing, Option[(Chunk[Actor], Stream[Pure, Actor])]] =
    avengersActors.pull.uncons
  val uncons1AvengersActors: Pull[Pure, Nothing, Option[(Actor, Stream[Pure, Actor])]]       =
    avengersActors.pull.uncons1

  def takeByName(name: String): Pipe[IO, Actor, Actor] = {
    def go(s: Stream[IO, Actor], name: String): Pull[IO, Actor, Unit] =
      s.pull.uncons1.flatMap {
        case Some((hd, tl)) =>
          if (hd.firstName == name) Pull.output1(hd) >> go(tl, name)
          else go(tl, name)
        case None           => Pull.done
      }
    in => go(in, name).stream
  }

  val avengersActorsCalledChris: Stream[IO, Unit] =
    avengersActors.through(takeByName("Chris")).through(toConsole)

  // override def run: IO[Unit] = for {
  //   _ <- IO.println("------------------------------------------------------")
  //   _ <- avengersActorsCalledChris.compile.drain
  //   _ <- IO.println("------------------------------------------------------")
  // } yield ()

  // 7. Using Concurrency in Streams

  val concurrentJlActors: Stream[IO, Actor]       = liftedJlActors.evalMap(actor =>
    IO {
      Thread.sleep(400)
      actor
    }
  )
  val liftedAvengersActors: Stream[IO, Actor]     = avengersActors.covary[IO]
  val concurrentAvengersActors: Stream[IO, Actor] = liftedAvengersActors.evalMap(actor =>
    IO {
      Thread.sleep(200)
      actor
    }
  )
  val mergedHeroesActors: Stream[IO, Unit]        =
    concurrentJlActors.merge(concurrentAvengersActors).through(toConsole)

  val queue: IO[Queue[IO, Actor]]           = Queue.bounded[IO, Actor](10)
  val concurrentlyStreams: Stream[IO, Unit] = Stream.eval(queue).flatMap { q =>
    val producer: Stream[IO, Unit] =
      liftedJlActors
        .evalTap(actor => IO.println(s"[${Thread.currentThread().getName}] produced $actor"))
        .evalMap(q.offer)
        .metered(1.second)
    val consumer: Stream[IO, Unit] =
      Stream
        .fromQueueUnterminated(q)
        .evalMap(actor => IO.println(s"[${Thread.currentThread().getName}] consumed $actor"))
    producer.concurrently(consumer)
  }

  val toConsoleWithThread: Pipe[IO, Actor, Unit] = in =>
    in.evalMap(actor => IO.println(s"[${Thread.currentThread().getName}] consumed $actor"))

  val parJoinedActors: Stream[IO, Unit] =
    Stream(
      jlActors.through(toConsoleWithThread),
      avengersActors.through(toConsoleWithThread),
      spiderMen.through(toConsoleWithThread)
    ).parJoin(4)

  // fs2 library code
  // implicit final class NestedStreamOps[F[_], O](private val outer: Stream[F, Stream[F, O]]) extends AnyVal {
  //   def parJoin(maxOpen: Int)(implicit F: Concurrent[F]): Stream[F, O] = ???
  // }

  override def run: IO[Unit] = for {
    _ <- IO.println("------------------------------------------------------")
    _ <- parJoinedActors.compile.drain
    _ <- IO.println("------------------------------------------------------")
  } yield ()
}
