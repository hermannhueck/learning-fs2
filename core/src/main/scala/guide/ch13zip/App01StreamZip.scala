package guide.ch13zip

import fs2.Stream
import munit.Assertions._

object App01StreamZip extends hutil.App {

  val zipped: List[(Int, Int)] = Stream(1, 2, 3).zip(Stream(4, 5, 6, 7)).toList
  println(zipped)
  assertEquals(zipped, List((1, 4), (2, 5), (3, 6)))
}
