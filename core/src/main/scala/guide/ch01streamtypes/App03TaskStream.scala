package guide.ch01streamtypes

import scala.concurrent.duration._

import fs2.Stream
import monix.eval.Task

import monix.execution.Scheduler.Implicits.global

object App03TaskStream extends hutil.App {

  println("----- Stream of Task effects")

  val stream: Stream[Task, Int] =
    Stream
      .eval(Task {
        println("BEING RUN!!"); 1 + 1
      })
      .repeat
      .take(3)

  val effect: Task[Vector[Int]] = stream.compile.toVector // Up to this point nothing is run

  val result: Vector[Int] = effect.runSyncUnsafe(3.seconds) // produces side effects (println) and returns the result

  println(result)
}
