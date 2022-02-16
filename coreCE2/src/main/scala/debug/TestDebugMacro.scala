package debug

import hutil.debug._
import hutil.stringformat._

/*
  http://www.warski.org/blog/2012/12/starting-with-scala-macros-a-short-tutorial/
  https://github.com/adamw/scala-macro-debug
 */
object TestDebugMacro extends hutil.App {

  val a = 10

  def f1(p: Int) = p + 1

  def test(): Unit = {

    def f2(p: Int) = p + 1

    s"$dash10 Macro debug1 $dash10".magenta.println()

    val y = 10

    debug1(y)
    debug1(y * 2)
    debug1(f1(y))

    s"$dash10 Macro debug $dash10".magenta.println()

    val b = 20
    val c = 30

    debug(b, c)
    debug("Got as far as here", a, b, c, f2(c))
    debug("Adding", a + b, "should be", c)
    debug(c)
    debug(f2(c))
  }

  test()
}
