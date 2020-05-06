package guide.ch13zip

import munit.Assertions._
import fs2.Stream

object App07StreamZipWithIndex extends hutil.App {

  val zipped: List[(String, Long)] = Stream("The", "quick", "brown", "fox").zipWithIndex.toList
  println(zipped)
  assertEquals(zipped, List(("The", 0L), ("quick", 1L), ("brown", 2L), ("fox", 3L)))
}
