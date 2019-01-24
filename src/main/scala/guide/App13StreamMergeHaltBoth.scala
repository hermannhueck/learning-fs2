package guide

import cats.effect.Concurrent
import fs2.Pipe2

import scala.language.higherKinds

object App13StreamMergeHaltBoth extends App {

  println("\n-----")

  /** Like `merge`, but halts as soon as _either_ branch halts. */
  def mergeHaltBoth[F[_] : Concurrent, O]: Pipe2[F, O, O, O] = (s1, s2) => ??? // TODO impl

  println("-----\n")
}
