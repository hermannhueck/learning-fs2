package guide

import fs2.{Pipe, Stream}

import scala.language.higherKinds

object App08aStreamTake extends App {

  println("\n-----")

  def take[F[_], O](n: Long): Pipe[F, O, O] =
    in => in.scanChunksOpt(n) { toTake =>
      if (toTake <= 0) None
      else Some(chunk => chunk.size match {
        case size if size < toTake => (toTake - size, chunk)
        case _ => (0, chunk.take(toTake.toInt))
      })
    }
  // tk: [F[_], O](n: Long)fs2.Pipe[F,O,O]

  val myRes = Stream(1,2,3,4).through(take(2)).toList
  // myRes: List[Int] = List(1, 2)
  println(myRes)

  val res = Stream(1,2,3,4).take(2).toList
  // res: List[Int] = List(1, 2)
  println(res)

  println("-----\n")
}
