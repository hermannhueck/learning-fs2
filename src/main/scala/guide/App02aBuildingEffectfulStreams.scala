package guide

import cats.effect.IO
import fs2.Stream

object App02aBuildingEffectfulStreams extends App {

  println("\n-----")

  val eff: Stream[IO, Int] = Stream.eval(IO { println("BEING RUN!!"); 1 + 1 })
  // eff: fs2.Stream[cats.effect.IO,Int] = Stream(..)

  // Any Stream formed using eval is called ‘effectful’ and can’t be run using toList or toVector
  // eff.toList
  // error: value toList is not a member of fs2.Stream[cats.effect.IO,Int]

  eff.compile.toVector.unsafeRunSync()
  //=> BEING RUN!!
  // res12: Vector[Int] = Vector(2)

  println("-----\n")
}
