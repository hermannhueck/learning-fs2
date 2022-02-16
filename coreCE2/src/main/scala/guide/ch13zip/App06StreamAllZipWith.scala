package guide.ch13zip

import fs2.Stream
import munit.Assertions._

object App06StreamAllZipWith extends hutil.App {

  val zipped: List[Int] = Stream(1, 2, 3).zipAllWith(Stream(4, 5, 6, 7))(0, 0)(_ + _).toList
  println(zipped)
  assertEquals(zipped, List(5, 7, 9, 7))
}
