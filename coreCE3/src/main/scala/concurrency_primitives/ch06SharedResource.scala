// see: https://fs2.io/#/concurrency-primitives - Shared Resopurce

package concurrency_primitives

import scala.concurrent.duration._

import cats.effect.std.Semaphore
import cats.effect.{IO, IOApp, Temporal}
import cats.syntax.all._
import fs2.Stream

class PreciousResource[F[_]: Temporal](name: String, s: Semaphore[F]) {

  def use: Stream[F, Unit] = {
    for {
      _ <- Stream.eval(s.available.map(a => println(s"$name >> Availability: $a")))
      _ <- Stream.eval(s.acquire)
      _ <- Stream.eval(s.available.map(a => println(s"$name >> Resource acquired | Availability: $a")))
      _ <- Stream.sleep(5.seconds)
      _ <- Stream.eval(s.available.map(a => println(s"$name >> Releasing resource ... | Availability: $a")))
      _ <- Stream.eval(s.release)
      _ <- Stream.eval(s.available.map(a => println(s"$name >> Done (Resource released) | Availability: $a")))
    } yield ()
  }
}

object Resources extends IOApp.Simple {

  def run: IO[Unit] = {
    val stream = for {
      s <- Stream.eval(Semaphore[IO](1))
      r1 = new PreciousResource[IO]("R1", s)
      r2 = new PreciousResource[IO]("R2", s)
      r3 = new PreciousResource[IO]("R3", s)
      _ <- Stream(r1.use, r2.use, r3.use).parJoin(3)
    } yield ()
    stream.compile.drain
  }
}
