package guide

import fs2.{INothing, Pure, Stream}

object App01BuildingPureStreams extends App {

  println("\n-----")

  val s0: Stream[Pure, INothing] = Stream.empty
  // s0: fs2.Stream[fs2.Pure,fs2.INothing] = Stream(..)

  val s1: Stream[Pure, Int] = Stream.emit(1)
  // s1: fs2.Stream[[x]fs2.Pure[x],Int] = Stream(..)

  val s1a: Stream[Pure, Int] = Stream(1, 2, 3) // variadic
  // s1a: fs2.Stream[[x]fs2.Pure[x],Int] = Stream(..)

  val s1b: Stream[Pure, Int] = Stream.emits(List(1, 2, 3)) // accepts any Seq
  // s1b: fs2.Stream[[x]fs2.Pure[x],Int] = Stream(..)


  // You can convert a pure stream to a List or Vector using:
  s1.toList
  // res0: List[Int] = List(1)
  println(s1.toList)
  println(s1.compile.toList) // on pure streams compile is a noop -> no need to compile

  s1.toVector
  // res1: Vector[Int] = Vector(1)

  // ‘list-like’ functions
  (Stream(1, 2, 3) ++ Stream(4, 5)).toList
  // res2: List[Int] = List(1, 2, 3, 4, 5)

  Stream(1, 2, 3).map(_ + 1).toList
  // res3: List[Int] = List(2, 3, 4)

  Stream(1, 2, 3).filter(_ % 2 != 0).toList
  // res4: List[Int] = List(1, 3)

  Stream(1, 2, 3).fold(0)(_ + _).toList
  // res5: List[Int] = List(6)

  Stream(None, Some(2), Some(3)).collect { case Some(i) => i }.toList
  // res6: List[Int] = List(2, 3)

  Stream.range(0, 5).intersperse(42).toList
  // res7: List[Int] = List(0, 42, 1, 42, 2, 42, 3, 42, 4)

  Stream(1, 2, 3).flatMap(i => Stream(i, i)).toList
  // res8: List[Int] = List(1, 1, 2, 2, 3, 3)

  // Stream(1,2,3).repeatN(2).toList
  // res10: List[Int] = List(1, 2, 3, 1, 2, 3)

  println("-----\n")
}
