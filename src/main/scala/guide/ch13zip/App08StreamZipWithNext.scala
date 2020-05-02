package guide.ch13zip

import munit.Assertions._
import fs2.Stream

object App08StreamZipWithNext extends hutil.App {

  val zipped: List[(String, Option[String])] = Stream("The", "quick", "brown", "fox").zipWithNext.toList
  println(zipped)
  assertEquals(zipped, List(("The", Some("quick")), ("quick", Some("brown")), ("brown", Some("fox")), ("fox", None)))
}
