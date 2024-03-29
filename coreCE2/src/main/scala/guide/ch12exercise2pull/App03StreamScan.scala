package guide.ch12exercise2pull

import fs2.{Chunk, INothing, Pipe, Pull, Stream}
import munit.Assertions._

object App03StreamScan extends hutil.App {

  implicit class exercise[+F[_], +O](stream: Stream[F, O]) {
    def myScan[O2](zero: O2)(f: (O2, O) => O2): Stream[F, O2] = stream.through(scan(zero)(f))
  }

  def scan[F[_], O, O2](zero: O2)(f: (O2, O) => O2): Pipe[F, O, O2] = {

    def go(stream: Stream[F, O], z: O2, count: Int): Pull[F, O2, Unit] = {

      val pull: Pull[F, INothing, Option[(Chunk[O], Stream[F, O])]] = stream.pull.uncons

      pull.flatMap {
        case None                          => Pull.done
        case Some((headChunk, tailStream)) =>
          def lastOf[E](chunk: Chunk[E]): E =
            chunk.drop(chunk.size - 1).head.get
          val ch: Chunk[O2]                 = headChunk.scanLeft(z)(f)
          val chTail                        = if (count == 0) ch else ch.drop(1)
          Pull.output(chTail) >> go(tailStream, lastOf(ch), count + 1)
      }
    }

    in => go(in, zero, 0).stream
  }

  val myRes = Stream.range(1, 10).myScan(0)(_ + _).toList
  // myRes: List[Int] = List(0, 1, 3, 6, 10, 15, 21, 28, 36, 45)
  println(myRes)

  val res = Stream.range(1, 10).scan(0)(_ + _).toList
  // res: List[Int] = List(0, 1, 3, 6, 10, 15, 21, 28, 36, 45)
  println(res)

  assertEquals(myRes, res)
}
