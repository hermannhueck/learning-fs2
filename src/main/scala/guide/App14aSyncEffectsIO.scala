package guide

import cats.effect.IO
import fs2.Stream

import scala.language.higherKinds

object App14aSyncEffectsIO extends App {

  println("\n-----")

  def destroyUniverse(): Unit = { println("BOOOOM!!!"); } // stub implementation

  val s = Stream.eval_(IO { destroyUniverse() }) ++ Stream("...moving on")
  // s: fs2.Stream[[x]cats.effect.IO[x],String] = Stream(..)

  val res = s.compile.toVector.unsafeRunSync()
  // BOOOOM!!!
  // res: Vector[String] = Vector(...moving on)

  println(res)

  println("-----\n")
}
