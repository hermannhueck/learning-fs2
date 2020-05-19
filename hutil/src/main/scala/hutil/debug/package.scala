package hutil

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

/*
  http://www.warski.org/blog/2012/12/starting-with-scala-macros-a-short-tutorial/
  https://github.com/adamw/scala-macro-debug
 */
package object debug {

  /*
    macro debug1, debugging a single expression
   */
  def debug1(param: Any): Unit = macro debug1_impl

  def debug1_impl(c: blackbox.Context)(param: c.Expr[Any]): c.Expr[Unit] = {
    import c.universe._
    val paramRep     = show(param.tree)
    val paramRepTree = Literal(Constant(paramRep))
    val paramRepExpr = c.Expr[String](paramRepTree)
    reify {
      println(paramRepExpr.splice + " = " + param.splice)
    }
  }

  def debug(params: Any*): Unit = macro debug_impl

  /*
    macro debug, debugging a Seq of expressions
   */
  def debug_impl(c: blackbox.Context)(params: c.Expr[Any]*): c.Expr[Unit] = {

    import c.universe._

    val trees = params.map { param =>
      param.tree match {
        // Keeping constants as-is
        // The c.universe prefixes aren't necessary, but otherwise Idea keeps importing weird stuff ...
        case c.universe.Literal(c.universe.Constant(_)) =>
          val reified = reify { print(param.splice) }
          reified.tree

        case _ =>
          val paramRep     = show(param.tree)
          val paramRepTree = Literal(Constant(paramRep))
          val paramRepExpr = c.Expr[String](paramRepTree)
          val reified = reify {
            print(paramRepExpr.splice + " = " + param.splice)
          }
          reified.tree
      }
    }

    // Inserting ", " between trees, and a println at the end.
    val separators = (1 until trees.size)
      .map(_ => reify { print(", ") }.tree) :+ reify { println() }.tree

    val treesWithSeparators =
      trees.zip(separators).flatMap(p => List(p._1, p._2))

    c.Expr[Unit](Block(treesWithSeparators.toList, Literal(Constant(()))))
  }
}
