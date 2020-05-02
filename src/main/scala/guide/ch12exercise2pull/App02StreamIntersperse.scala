package guide.ch12exercise2pull

import fs2.{Chunk, INothing, Pipe, Pull, Stream}

import munit.Assertions._

object App02StreamIntersperse extends hutil.App {

  implicit class exercise[+F[_], O](stream: Stream[F, O]) {
    def myIntersperse(separator: O): Stream[F, O] = stream.through(intersperse(separator))
  }

  def intersperse[F[_], O](separator: O): Pipe[F, O, O] = {

    def go(stream: Stream[F, O], sep: O): Pull[F, O, Unit] = {

      val pull: Pull[F, INothing, Option[(Chunk[O], Stream[F, O])]] = stream.pull.uncons

      pull.flatMap {
        case None => Pull.done
        case Some((headChunk, tailStream)) =>
          val chunk: Chunk[O] = headChunk.flatMap(elem => Chunk(sep, elem))
          Pull.output(chunk) >> go(tailStream, sep)
      }
    }

    in => go(in, separator).stream.tail
  }

  val myRes = Stream("Alice", "Bob", "Carol").myIntersperse("|").toList
  // myRes: List[String] = List(Alice, |, Bob, |, Carol)
  println(myRes)

  val res = Stream("Alice", "Bob", "Carol").intersperse("|").toList
  // res: List[String] = List(Alice, |, Bob, |, Carol)
  println(res)

  assertEquals(myRes, res)
}
