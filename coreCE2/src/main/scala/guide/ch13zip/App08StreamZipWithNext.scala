package guide.ch13zip

import fs2.Stream
import munit.Assertions._

object App08StreamZipWithNext extends hutil.App {

  val zipped: List[(String, Option[String])] = Stream("The", "quick", "brown", "fox").zipWithNext.toList
  println(zipped)
  assertEquals(zipped, List(("The", Some("quick")), ("quick", Some("brown")), ("brown", Some("fox")), ("fox", None)))
}
