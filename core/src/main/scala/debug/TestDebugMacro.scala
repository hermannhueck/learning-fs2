package debug

import hutil.debug._
import hutil.stringformat._

/*
  http://www.warski.org/blog/2012/12/starting-with-scala-macros-a-short-tutorial/
  https://github.com/adamw/scala-macro-debug
 */
object TestDebugMacro extends hutil.App {

  s"$dash10 Debug macro $dash10".magenta.println

  val a = 10

  def test(): Unit = {

    val b = 20
    val c = 30

    debug(b, c)
    debug("Got as far as here", a, b, c)
    debug("Adding", a + b, "should be", c)
  }

  test()
}
