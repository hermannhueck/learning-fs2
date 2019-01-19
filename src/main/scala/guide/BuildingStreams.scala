package guide

import fs2.{INothing, Pure, Stream}

object BuildingStreams extends App {

  val s0 = Stream.empty
  // s0: fs2.Stream[fs2.Pure,fs2.INothing] = Stream(..)

  val s1 = Stream.emit(1)
  // s1: fs2.Stream[[x]fs2.Pure[x],Int] = Stream(..)

  val s1a = Stream(1,2,3) // variadic
  // s1a: fs2.Stream[[x]fs2.Pure[x],Int] = Stream(..)

  val s1b = Stream.emits(List(1,2,3)) // accepts any Seq
  // s1b: fs2.Stream[[x]fs2.Pure[x],Int] = Stream(..)


  // You can convert a pure stream to a List or Vector using:
  s1.toList
  // res0: List[Int] = List(1)

  s1.toVector
  // res1: Vector[Int] = Vector(1)

  
}
