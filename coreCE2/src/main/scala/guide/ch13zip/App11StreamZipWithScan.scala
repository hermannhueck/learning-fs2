package guide.ch13zip

import fs2.Stream
import munit.Assertions._

object App11StreamZipWithScan extends hutil.App {

  val zipped: List[(String, Int)] = Stream("uno", "dos", "tres", "cuatro").zipWithScan(0)(_ + _.length).toList
  println(zipped)
  assertEquals(zipped, List(("uno", 0), ("dos", 3), ("tres", 6), ("cuatro", 10)))
}
