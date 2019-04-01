package mycode.ch01aStreamTypes

import cats.effect.{IO, Sync}
import fs2.Stream
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global

import scala.concurrent.duration._

object App04FStream extends App {

  println("\n----- Stream of generic effects")

  def stream[F[_]: Sync]: Stream[F, Int] = Stream.eval(Sync[F].delay {
    println("BEING RUN!!"); 1 + 1
  }).repeat.take(3)

  def effect[F[_]: Sync]: F[Vector[Int]] = stream.compile.toVector   // Up to this point nothing is run


  println(">>> reify F with cats.effect.IO:")
  val ioEffect: IO[Vector[Int]] = effect[IO]
  val ioResult: Vector[Int] = ioEffect.unsafeRunSync()    // produces side effects (println) and returns the result
  println(ioResult)


  println("\n>>> reify F with monix.eval.Task:")
  val taskEffect: Task[Vector[Int]] = effect[Task]
  val taskResult: Vector[Int] = taskEffect.runSyncUnsafe(3.seconds)    // produces side effects (println) and returns the result
  println(taskResult)

  println("-----\n")
}
