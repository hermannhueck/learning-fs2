package guide

import fs2._
import cats.effect._

trait Connection {
  def readBytes(onSuccess: Array[Byte] => Unit, onFailure: Throwable => Unit): Unit

  // or perhaps
  def readBytesE(onComplete: Either[Throwable, Array[Byte]] => Unit): Unit =
    readBytes(bs => onComplete(Right(bs)), e => onComplete(Left(e)))

  override def toString = "<connection>"
}

object Ch12bAyncEffectsOneCB extends IOApp.Simple {

  val connection = new Connection {
    def readBytes(onSuccess: Array[Byte] => Unit, onFailure: Throwable => Unit): Unit = {
      Thread.sleep(200)
      onSuccess(Array(0, 1, 2))
    }
  }

  val bytes: IO[Array[Byte]] = IO.async_[Array[Byte]] { cb => connection.readBytesE(cb) }

  val stream: Stream[IO, List[Byte]] = Stream.eval(bytes).map(_.toList)

  val run: IO[Unit] = for {
    _ <- IO.println("-----------------------------------")
    _ <- stream.evalMap(IO.println).compile.drain
    _ <- IO.println("-----------------------------------")
  } yield ()
}
