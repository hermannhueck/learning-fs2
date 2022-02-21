// see: https://fs2.io/#/guide - Exercises Stream Transforming

import fs2._
import munit.Assertions._

implicit class exerciseTakeWhile[+F[_], +O](stream: Stream[F, O]) {
  def myTakeWhile_1(predicate: O => Boolean): Stream[F, O] = stream.through(takeWhile_1(predicate))
  def myTakeWhile_2(predicate: O => Boolean): Stream[F, O] = stream.through(takeWhile_2(predicate))
}

def takeWhile_1[F[_], O](predicate: O => Boolean): Pipe[F, O, O] = {

  def go(stream: Stream[F, O], p: O => Boolean): Pull[F, O, Unit] = {

    val pull: Pull[F, Nothing, Option[(Chunk[O], Stream[F, O])]] = stream.pull.uncons

    pull.flatMap {
      case None                          => Pull.done
      case Some((headChunk, tailStream)) =>
        val vec: Vector[O] = headChunk.toVector.takeWhile(p)
        if (headChunk.size - vec.size > 0)
          Pull.output(Chunk.vector(vec)) >> Pull.done
        else
          Pull.output(headChunk) >> go(tailStream, p)
    }
  }

  in => go(in, predicate).stream
}

def takeWhile_2[F[_], O](predicate: O => Boolean): Pipe[F, O, O] = { in: Stream[F, O] =>
  type State = Boolean
  in.scanChunksOpt(true) {
    case false => None
    case true  =>
      val function: Chunk[O] => (State, Chunk[O]) = chunk => {
        val newChunk: Chunk[O] = Chunk.vector(chunk.toVector.takeWhile(predicate))
        (newChunk.size == chunk.size, newChunk)
      }
      Some(function)
  }
}

val resTakeWhile = Stream.range(0, 100).takeWhile(_ < 7).toList
// res: List[Int] = List(0, 1, 2, 3, 4, 5, 6)
println(resTakeWhile)

val myResTakeWhile1 = Stream.range(0, 100).myTakeWhile_1(_ < 7).toList
// myResTakeWhile1: List[Int] = List(0, 1, 2, 3, 4, 5, 6)
println(myResTakeWhile1)

myResTakeWhile1 == resTakeWhile
assertEquals(myResTakeWhile1, resTakeWhile)

val myResTakeWhile2 = Stream.range(0, 100).myTakeWhile_2(_ < 7).toList
// myResTakeWhile2: List[Int] = List(0, 1, 2, 3, 4, 5, 6)
println(myResTakeWhile2)

myResTakeWhile2 == resTakeWhile
assertEquals(myResTakeWhile2, resTakeWhile)

implicit class exerciseIntersperse[+F[_], O](stream: Stream[F, O]) {
  def myIntersperse_1(separator: O): Stream[F, O] = stream.through(intersperse_1(separator))
  def myIntersperse_2(separator: O): Stream[F, O] = stream.through(intersperse_2(separator))
  def myIntersperse_3(separator: O): Stream[F, O] = stream.through(intersperse_3(separator))
  def myIntersperse_4(separator: O): Stream[F, O] = stream.through(intersperse_4(separator))
  def myIntersperse_5(separator: O): Stream[F, O] = stream.through(intersperse_5(separator))
}

def intersperse_1[F[_], O](separator: O): Pipe[F, O, O] = {

  def go(stream: Stream[F, O], sep: O): Pull[F, O, Unit] = {

    val pull: Pull[F, Nothing, Option[(Chunk[O], Stream[F, O])]] = stream.pull.uncons

    pull.flatMap {
      case None                          => Pull.done
      case Some((headChunk, tailStream)) =>
        val chunk: Chunk[O] = headChunk.flatMap(elem => Chunk(sep, elem))
        Pull.output(chunk) >> go(tailStream, sep)
    }
  }

  in => go(in, separator).stream.tail
}

def intersperse_2[F[_], O](separator: O): Pipe[F, O, O] = { in: Stream[F, O] =>
  type State = O
  in.scanChunksOpt(separator) { sep: State =>
    val function: Chunk[O] => (State, Chunk[O]) =
      chunk => (sep, chunk.flatMap(o => Chunk(sep, o)))
    Some(function)
  }.tail // remove leading separator
}

def intersperse_3[F[_], O](separator: O): Pipe[F, O, O] = { in: Stream[F, O] =>
  type State = Unit // we don't really need a state for intersperse
  in.scanChunksOpt(()) { _: State =>
    val function: Chunk[O] => (State, Chunk[O]) =
      chunk => ((), chunk.flatMap(o => Chunk(separator, o)))
    Some(function)
  }.tail // remove leading separator
}

def intersperse_4[F[_], O](separator: O): Pipe[F, O, O] = { in: Stream[F, O] =>
  type State = Unit // we don't really need a state for intersperse
  in.scanChunks(()) { // scanChunks is sufficient, as we scan all Chunks
    val function: (State, Chunk[O]) => (State, Chunk[O]) =
      (_, chunk) => ((), chunk.flatMap(o => Chunk(separator, o)))
    function
  }.tail // remove leading separator
}

def intersperse_5[F[_], O](separator: O): Pipe[F, O, O] =
  in => in.flatMap { elem => Stream(separator, elem) }.tail // remove leading separator

val resIntersperse = Stream("Alice", "Bob", "Carol").intersperse("|").toList
// resIntersperse: List[String] = List(Alice, |, Bob, |, Carol)
println(resIntersperse)

val myResIntersperse_1 = Stream("Alice", "Bob", "Carol").myIntersperse_1("|").toList
// myResIntersperse_1: List[String] = List(Alice, |, Bob, |, Carol)
println(myResIntersperse_1)
myResIntersperse_1 == resIntersperse
assertEquals(myResIntersperse_1, resIntersperse)

val myResIntersperse_2 = Stream("Alice", "Bob", "Carol").myIntersperse_2("|").toList
// myResIntersperse_2: List[String] = List(Alice, |, Bob, |, Carol)
println(myResIntersperse_2)
myResIntersperse_2 == resIntersperse
assertEquals(myResIntersperse_2, resIntersperse)

val myResIntersperse_3 = Stream("Alice", "Bob", "Carol").myIntersperse_3("|").toList
// myResIntersperse_3: List[String] = List(Alice, |, Bob, |, Carol)
println(myResIntersperse_3)
myResIntersperse_3 == resIntersperse
assertEquals(myResIntersperse_3, resIntersperse)

val myResIntersperse_4 = Stream("Alice", "Bob", "Carol").myIntersperse_4("|").toList
// myResIntersperse_4: List[String] = List(Alice, |, Bob, |, Carol)
println(myResIntersperse_4)
myResIntersperse_4 == resIntersperse
assertEquals(myResIntersperse_4, resIntersperse)

val myResIntersperse_5 = Stream("Alice", "Bob", "Carol").myIntersperse_5("|").toList
// myResIntersperse_5: List[String] = List(Alice, |, Bob, |, Carol)
println(myResIntersperse_5)
myResIntersperse_5 == resIntersperse
assertEquals(myResIntersperse_5, resIntersperse)

implicit class exerciseScan[+F[_], +O](stream: Stream[F, O]) {
  def myScan[O2](zero: O2)(f: (O2, O) => O2): Stream[F, O2] = stream.through(scan(zero)(f))
}

def scan[F[_], O, O2](zero: O2)(f: (O2, O) => O2): Pipe[F, O, O2] = {

  def go(stream: Stream[F, O], z: O2, count: Int): Pull[F, O2, Unit] = {

    val pull: Pull[F, Nothing, Option[(Chunk[O], Stream[F, O])]] = stream.pull.uncons

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

val resScan = Stream.range(1, 10).scan(0)(_ + _).toList
// resScan: List[Int] = List(0, 1, 3, 6, 10, 15, 21, 28, 36, 45)
println(resScan)

val myResScan = Stream.range(1, 10).myScan(0)(_ + _).toList
// myResScan: List[Int] = List(0, 1, 3, 6, 10, 15, 21, 28, 36, 45)
println(myResScan)

myResScan == resScan
assertEquals(myResScan, resScan)
