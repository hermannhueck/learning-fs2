package guide.ch19talkingtotheoutsideworld

import cats.effect.{Async, IO}
import fs2.Stream
import hutil.stringformat._

object App02bAsyncEffectsParametric extends hutil.App {

  trait Connection {

    def readBytes(onSuccess: Array[Byte] => Unit, onFailure: Throwable => Unit): Unit

    def readBytesE(onComplete: Either[Throwable, Array[Byte]] => Unit): Unit =
      readBytes(bs => onComplete(Right(bs)), e => onComplete(Left(e)))

    override def toString = "<connection>"
  }

  val connection = new Connection {
    def readBytes(onSuccess: Array[Byte] => Unit, onFailure: Throwable => Unit): Unit = {
      Thread sleep 200L
      onSuccess(Array(0, 1, 2))
    }
  }

  def fBytes[F[_]: Async]: F[Array[Byte]] = Async[F].async[Array[Byte]] {
    (callback: Either[Throwable, Array[Byte]] => Unit) => connection.readBytesE(callback)
  }
  // fBytes: [F[_]](implicit evidence$1: cats.effect.Async[F])F[Array[Byte]]

  val ioBytes: IO[Array[Byte]] = fBytes[IO]
  // ioBytes: cats.effect.IO[Array[Byte]] = IO$425428304

  ">>> Evaluate IO directly ...".magenta.println
  val ioRes =
    ioBytes
      .unsafeRunSync
      .toList
  // ioRes: List[Byte] = List(0, 1, 2)
  println(ioRes)

  ">>> Evaluate IO in a Stream ...".magenta.println
  val streamRes =
    Stream
      .eval(ioBytes)
      .map(_.toList)
      .compile
      .toVector
      .unsafeRunSync
  // streamRes: Vector[List[Byte]] = Vector(List(0, 1, 2))
  println(streamRes)
  println(streamRes.toList.flatten)
}
