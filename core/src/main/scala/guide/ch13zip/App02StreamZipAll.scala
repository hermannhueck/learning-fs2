package guide.ch13zip

import munit.Assertions._
import fs2.Stream

object App02StreamZipAll extends hutil.App {

  val zipped: List[(Int, Int)] = Stream(1, 2, 3).zipAll(Stream(4, 5, 6, 7))(0, 0).toList
  println(zipped)
  assertEquals(zipped, List((1, 4), (2, 5), (3, 6), (0, 7)))
}
