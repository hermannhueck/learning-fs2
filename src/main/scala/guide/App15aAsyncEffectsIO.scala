package guide

import cats.effect.IO
import fs2.Stream

import scala.language.higherKinds

object App15aAsyncEffectsIO extends App {

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

  val bytes = IO.async[Array[Byte]] { (cb: Either[Throwable, Array[Byte]] => Unit) =>
    c.readBytesE(cb)
  }
  // bytes: cats.effect.IO[Array[Byte]] = IO$425428304

  val res = Stream.eval(bytes).map(_.toList).compile.toVector.unsafeRunSync()
  // res: Vector[List[Byte]] = Vector(List(0, 1, 2))

  println(res)

  println("-----\n")
}
