package guide

import fs2.{Chunk, Pipe, Pull, Pure, Stream}

import scala.language.higherKinds

object App09StreamTakeWhile extends App {

  println("\n-----")

  def takeWhile[F[_], O](pred: O => Boolean): Pipe[F, O, O] = {

    def go(s: Stream[F, O], p: O => Boolean): Pull[F, O, Unit] = {
      s.pull.uncons.flatMap {
        case Some((head, tail)) =>
          val vec = head.toVector.takeWhile(p)
          head.size - vec.size match {
            case diff if diff > 0 => Pull.output(Chunk.vector(vec)) >> Pull.done
            case _ => Pull.output(head) >> go(tail, p)
          }
        case None => Pull.done
      }
    }

    in => go(in, pred).stream
  }

  val myRes = Stream.range(0,100).through(takeWhile(_ < 7)).toList
  // myRes: List[Int] = List(0, 1, 2, 3, 4, 5, 6)
  println(myRes)

  val res = Stream.range(0,100).takeWhile(_ < 7).toList
  // res: List[Int] = List(0, 1, 2, 3, 4, 5, 6)
  println(res)

  println("-----\n")
}
