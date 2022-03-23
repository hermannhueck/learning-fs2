package guide

import fs2._
import cats.effect._

object Ch12aSyncEffects extends IOApp.Simple {

  def destroyUniverse(): Unit = { println("BOOOOM!!!"); } // stub implementation

  val stream: Stream[IO, String] = Stream.exec(Sync[IO].delay { destroyUniverse() }) ++ Stream("...moving on")

  val run: IO[Unit] = for {
    _ <- IO.println("-----------------------------------")
    _ <- stream.evalMap(IO.println).compile.drain
    _ <- IO.println("-----------------------------------")
  } yield ()
}
