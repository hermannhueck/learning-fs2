package guide

import cats.effect.{Async, IO}
import fs2.Stream

import scala.language.higherKinds

object App15bAsyncEffectsParametric extends App {

  println("\n-----")

  trait Connection {

    def readBytes(onSuccess: Array[Byte] => Unit, onFailure: Throwable => Unit): Unit

    def readBytesE(onComplete: Either[Throwable, Array[Byte]] => Unit): Unit =
      readBytes(bs => onComplete(Right(bs)), e => onComplete(Left(e)))

    override def toString = "<connection>"
  }

  val c = new Connection {
    def readBytes(onSuccess: Array[Byte] => Unit, onFailure: Throwable => Unit): Unit = {
      Thread.sleep(200)
      onSuccess(Array(0,1,2))
    }
  }

  def bytes[F[_] : Async]: F[Array[Byte]] = Async[F].async[Array[Byte]] { (cb: Either[Throwable, Array[Byte]] => Unit) =>
    c.readBytesE(cb)
  }
  // bytes: [F[_]](implicit evidence$1: cats.effect.Async[F])F[Array[Byte]]

  val res = Stream.eval(bytes[IO]).map(_.toList).compile.toVector.unsafeRunSync()
  // res: Vector[List[Byte]] = Vector(List(0, 1, 2))

  println(res)

  println("-----\n")
}
