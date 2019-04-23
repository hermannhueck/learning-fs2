package guide.ch13zip

import cats.effect.{ContextShift, IO, Timer}
import fs2.Stream

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object App04StreamZipRight extends App {

  println("\n-----")

  val zippedLeft: List[Int] = Stream(1, 2, 3).zipRight(Stream(4, 5, 6, 7)).toList
  println(zippedLeft)
  assert(zippedLeft == List(4, 5, 6))


  private val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  implicit val cs: ContextShift[IO] = IO.contextShift(ec)
  implicit val timer: Timer[IO] = IO.timer(ec)

  val stream = Stream.fixedDelay(300.millis) zipRight Stream.range(0, 5)
  val vec = stream.compile.toVector.unsafeRunSync
  println(vec)
  assert(vec == Vector(0, 1, 2, 3, 4))

  println("-----\n")
}
