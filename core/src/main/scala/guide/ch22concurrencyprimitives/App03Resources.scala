package guide.ch22concurrencyprimitives

import scala.concurrent.duration._

import cats.effect._
import cats.effect.concurrent.Semaphore
import cats.syntax.functor._
import fs2.Stream

class PreciousResource[F[_]: Concurrent: Timer](name: String, s: Semaphore[F]) {

  def use: Stream[F, Unit] = {
    for {
      _ <- Stream.eval(s.available.map(a => println(s"$name >> Availability: $a")))
      _ <- Stream.eval(s.acquire)
      _ <- Stream.eval(s.available.map(a => println(s"$name >> Started | Availability: $a")))
      _ <- Stream.sleep(3.seconds)
      _ <- Stream.eval(s.release)
      _ <- Stream.eval(s.available.map(a => println(s"$name >> Done | Availability: $a")))
    } yield ()
  }
}

object App03Resources extends hutil.IOApp {

  @scala.annotation.nowarn("cat=w-flag-dead-code&msg=dead code following this construct:ws")
  val stream = for {
    s  <- Stream.eval(Semaphore[IO](1))
    r1 = new PreciousResource[IO]("R1", s)
    r2 = new PreciousResource[IO]("R2", s)
    r3 = new PreciousResource[IO]("R3", s)
    _  <- Stream(r1.use, r2.use, r3.use).parJoin(3).drain
  } yield ()

  override def ioRun(args: List[String]): IO[ExitCode] = {
    stream.compile.drain.as(ExitCode.Success)
  }
}
