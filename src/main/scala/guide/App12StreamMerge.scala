package guide

import cats.effect.IO
import fs2.{Chunk, Pipe, Pull, Stream}

import scala.language.higherKinds

object App12StreamMerge extends App {

  println("\n-----")

  import cats.effect.ContextShift

  // This normally comes from IOApp
  implicit val ioContextShift: ContextShift[IO] = IO.contextShift(scala.concurrent.ExecutionContext.Implicits.global)
  // ioContextShift: cats.effect.ContextShift[cats.effect.IO] = cats.effect.internals.IOContextShift@eb64a2b

  // The merge function runs two streams concurrently, combining their outputs. It halts when both inputs have halted:
  val res = Stream(1,2,3).merge(Stream.eval(IO { Thread.sleep(200); 4 })).compile.toVector.unsafeRunSync()
  // res: Vector[Int] = Vector(1, 2, 3, 4)
  println(res)

  println("-----\n")
}
