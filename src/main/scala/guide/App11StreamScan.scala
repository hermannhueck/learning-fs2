package guide

import fs2.{Chunk, Pipe, Pull, Stream}

import scala.language.higherKinds

object App11StreamScan extends App {

  println("\n-----")

  def scan[F[_], O, O2](seed: O2)(f: (O2, O) => O2): Pipe[F, O, O2] = {

    def go(s: Stream[F, O], z: O2, count: Int): Pull[F, O2, Unit] =
      s.pull.uncons.flatMap {
        case None => Pull.done
        case Some((head, tail)) =>
          val vec = head.toVector.scanLeft(z)(f)
          //println(s"vec >>> $vec")
          val vecTail = if (count == 0) vec else vec.tail
          Pull.output(Chunk.vector(vecTail)) >> go(tail, vec.last, count+1)
      }

    in => go(in, seed, 0).stream
  }

  val myRes = Stream.range(1,10).through(scan(0)(_ + _)).toList
  // myRes: List[String] = List(Alice, |, Bob, |, Carol)
  println(myRes)

  val res = Stream.range(1,10).scan(0)(_ + _).toList
  // res: List[String] = List(Alice, |, Bob, |, Carol)
  println(res)

  println("-----\n")
}
