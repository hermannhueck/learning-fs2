package guide.ch13zip

import fs2.Stream
import munit.Assertions._

object App05StreamZipWith extends hutil.App {

  val zipped: List[Int] = Stream(1, 2, 3).zipWith(Stream(4, 5, 6, 7))(_ + _).toList
  println(zipped)
  assertEquals(zipped, List(5, 7, 9))
}
