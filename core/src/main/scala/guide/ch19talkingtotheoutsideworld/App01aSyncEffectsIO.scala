package guide.ch19talkingtotheoutsideworld

import cats.effect.IO
import fs2.Stream

object App01aSyncEffectsIO extends hutil.App {

  def destroyUniverse(): Unit = { println("BOOOOM!!!"); } // stub implementation

  // Stream.eval_
  // Creates a stream that evaluates the supplied fa for its effect, discarding the output value.

  val s =
    Stream.eval_(IO { destroyUniverse() }) ++
      Stream("...moving on")
  // s: fs2.Stream[[x]cats.effect.IO[x],String] = Stream(..)

  val res = s.compile.toVector.unsafeRunSync
  // BOOOOM!!!
  // res: Vector[String] = Vector(...moving on)

  println(res)
}
