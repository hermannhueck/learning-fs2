package guide.ch13zip

import munit.Assertions._
import fs2.Stream

object App09StreamZipWithPrevious extends hutil.App {

  val zipped: List[(Option[String], String)] = Stream("The", "quick", "brown", "fox").zipWithPrevious.toList
  println(zipped)
  assertEquals(zipped, List((None, "The"), (Some("The"), "quick"), (Some("quick"), "brown"), (Some("brown"), "fox")))
}
