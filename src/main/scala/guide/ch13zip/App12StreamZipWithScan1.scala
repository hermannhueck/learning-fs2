package guide.ch13zip

import munit.Assertions._
import fs2.Stream

object App12StreamZipWithScan1 extends hutil.App {

  val zipped: List[(String, Int)] = Stream("uno", "dos", "tres", "cuatro").zipWithScan1(0)(_ + _.length).toList
  println(zipped)
  assertEquals(zipped, List(("uno", 3), ("dos", 6), ("tres", 10), ("cuatro", 16)))
}
