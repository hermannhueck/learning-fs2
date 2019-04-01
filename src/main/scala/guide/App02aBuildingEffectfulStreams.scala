package guide

import cats.effect.{IO, Sync}
import fs2.Stream
import monix.eval.Task
import monix.execution.Scheduler

object App02aBuildingEffectfulStreams extends App {

  println("\n----- Stream of IO effects")

  {
    val eff: Stream[IO, Int] = Stream.eval(IO {
      println("BEING RUN!!"); 1 + 1
    })
    // eff: fs2.Stream[cats.effect.IO,Int] = Stream(..)

    // Any Stream formed using eval is called ‘effectful’ and can’t be run using toList or toVector
    // eff.toList
    // error: value toList is not a member of fs2.Stream[cats.effect.IO,Int]

    eff.compile.toVector.unsafeRunSync()
    //=> BEING RUN!!
    // res12: Vector[Int] = Vector(2)
  }

  println("\n----- Stream of Task effects")

  {
    val eff: Stream[Task, Int] = Stream.eval(Task { println("BEING RUN!!"); 1 + 1 })
    // eff: fs2.Stream[cats.effect.IO,Int] = Stream(..)

    // Any Stream formed using eval is called ‘effectful’ and can’t be run using toList or toVector
    // eff.toList
    // error: value toList is not a member of fs2.Stream[cats.effect.IO,Int]

    implicit val scheduler: Scheduler = Scheduler.global
    eff.compile.toVector.runToFuture
    //=> BEING RUN!!
    // res12: Vector[Int] = Vector(2)
  }

  println("\n----- Stream of generic effect type:  [F[_]: Sync]")

  {
    def eff[F[_]: Sync]: Stream[F, Int] = Stream.eval(Sync[F].delay { println("BEING RUN!!"); 1 + 1 })
    // eff: fs2.Stream[cats.effect.IO,Int] = Stream(..)

    // Any Stream formed using eval is called ‘effectful’ and can’t be run using toList or toVector
    // eff.toList
    // error: value toList is not a member of fs2.Stream[cats.effect.IO,Int]

    println(">>> reify F with IO")
    eff[IO].compile.toVector.unsafeRunSync()

    println(">>> reify F with Task")
    implicit val scheduler: Scheduler = Scheduler.global
    eff[Task].compile.toVector.runToFuture
    //=> BEING RUN!!
    // res12: Vector[Int] = Vector(2)
  }

  println("-----\n")
}
