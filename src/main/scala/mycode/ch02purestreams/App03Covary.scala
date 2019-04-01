package mycode.ch02PureStreams

import cats.effect.{IO, Sync}
import fs2.{Pure, Stream}
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global

object App04Covary extends App {

  println("\n----- convert pure Stream to effectful Stream with Stream#covary")

  val pureStream: Stream[Pure, Int] = Stream.range(0, 10)

  println(">>> covary pure Stream to IO")
  val ioStream: Stream[IO, Int] = pureStream.covary[IO]
  println(ioStream.compile.toVector.unsafeRunSync())

  println(">>> covary pure Stream to Task")
  val taskStream: Stream[Task, Int] = pureStream.covary[Task]
  println(taskStream.compile.toVector.runSyncUnsafe())

  println(">>> covary pure Stream to F[_]: Sync")
  def fStream[F[_]: Sync]: Stream[F, Int] = pureStream.covary[F]
  def effect[F[_]: Sync]: F[Vector[Int]] = fStream.compile.toVector
  println(effect[IO].unsafeRunSync())
  println(effect[Task].runSyncUnsafe())

  println("-----\n")
}
