package guide.ch12exercise2pull

import fs2.Chunk
import fs2.Pipe
import fs2.Stream
import munit.Assertions._

object App01StreamTakeWhileWithoutPull extends hutil.App {

  implicit class exercise[+F[_], +O](stream: Stream[F, O]) {
    def myTakeWhile(predicate: O => Boolean): Stream[F, O] = stream.through(takeWhile(predicate))
  }

  def takeWhile[F[_], O](predicate: O => Boolean): Pipe[F, O, O] = { in: Stream[F, O] =>
    type State = Boolean
    in.scanChunksOpt(true) {
      case false => None
      case true =>
        val function: Chunk[O] => (State, Chunk[O]) = chunk => {
          val newChunk: Chunk[O] = Chunk.vector(chunk.toVector.takeWhile(predicate))
          (newChunk.size == chunk.size, newChunk)
        }
        Some(function)
    }
  }

  val myRes = Stream.range(0, 100).through(takeWhile(_ < 7)).toList
  // myRes: List[Int] = List(0, 1, 2, 3, 4, 5, 6)
  println(myRes)

  val res = Stream.range(0, 100).takeWhile(_ < 7).toList
  // res: List[Int] = List(0, 1, 2, 3, 4, 5, 6)
  println(res)

  assertEquals(myRes, res)
}
