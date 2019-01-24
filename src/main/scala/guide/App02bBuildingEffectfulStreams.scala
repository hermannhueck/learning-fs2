package guide

import cats.effect.IO
import fs2.Stream

object App02bBuildingEffectfulStreams extends App {

  println("\n-----")

  val eff: Stream[IO, Int] = Stream.eval(IO { println("TASK BEING RUN!!"); 1 + 1 })

  // The first .compile.toVector is one of several methods available to ‘compile’ the stream to a single effect:
  
  val ra = eff.compile.toVector // gather all output into a Vector
  // ra: cats.effect.IO[Vector[Int]] = <function1>

  val rb = eff.compile.drain // purely for effects
  // rb: cats.effect.IO[Unit] = <function1>

  val rc = eff.compile.fold(0)(_ + _) // run and accumulate some result
  // rc: cats.effect.IO[Int] = <function1>


  ra.unsafeRunSync()
  //=> TASK BEING RUN!!
  // res13: Vector[Int] = Vector(2)

  rb.unsafeRunSync()
  //=> TASK BEING RUN!!

  rc.unsafeRunSync()
  //=> TASK BEING RUN!!
  // res15: Int = 2

  rc.unsafeRunSync()
  //=> TASK BEING RUN!!
  // res16: Int = 2
  
  println("-----\n")
}
