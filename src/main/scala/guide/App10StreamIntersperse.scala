package guide

import fs2.{Chunk, Pipe, Pull, Stream}

import scala.language.higherKinds

object App10StreamIntersperse extends App {

  println("\n-----")

  def intersperse[F[_], O](separator: O): Pipe[F, O, O] = {

    def go(s: Stream[F, O], sep: O): Pull[F, O, Unit] = {
      s.pull.uncons.flatMap {
        case Some((head, tail)) =>
          val vec = head.toVector.flatMap(elem => Vector(sep, elem))
          Pull.output(Chunk.vector(vec)) >> go(tail, sep)
        case None => Pull.done
      }
    }

    in => go(in, separator).stream.tail
  }

  val myRes = Stream("Alice","Bob","Carol").through(intersperse("|")).toList
  // myRes: List[String] = List(Alice, |, Bob, |, Carol)
  println(myRes)

  val res = Stream("Alice","Bob","Carol").intersperse("|").toList
  // res: List[String] = List(Alice, |, Bob, |, Carol)
  println(res)

  println("-----\n")
}
