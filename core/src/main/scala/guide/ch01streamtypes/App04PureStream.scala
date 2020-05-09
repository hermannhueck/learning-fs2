package guide.ch01streamtypes

import cats.Id
import fs2.{Pure, Stream}

object App04PureStream extends hutil.App {

  println("----- Stream of Pure values without effects")

  val stream: Stream[Pure, Int] =
    Stream.emit(1 + 1).repeat.take(3)

  val result: Id[Vector[Int]] = stream.compile.toVector
  val result2                 = stream.toVector // compile step can be omitted for pure streams

  // pure stream needs not be run!

  println(result)
  println(result2)
}
