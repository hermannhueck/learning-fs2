package tools

import java.net.URL

import scala.annotation.tailrec
import scala.io.Source

import scala.language.reflectiveCalls

trait Tools {

  final def sumOfRange(from: Int, to: Int): Int =
    (from until to).toList.sum

  @tailrec
  final def fibonacci(cycles: Int, a: BigInt = 0, b: BigInt = 1): BigInt = {
    if (cycles > 0)
      fibonacci(cycles - 1, b, a + b)
    else
      b
  }

  def factorial(n: Int): BigInt = {

    @tailrec
    def fac(n2: Int, acc: BigInt): BigInt =
      if (n2 == 0)
        acc
      else
        fac(n2 - 1, n2 * acc)

    if (n < 0)
      throw new IllegalArgumentException(s"factorial of $n not defined") // scalafix:ok DisableSyntax.throw
    else
      fac(n, BigInt(1))
  }

  def currentThread: String = "Thread: " + Thread.currentThread.getName

  def printCurrentThread(): Unit = println(currentThread)

  def wordCount(limit: Int): List[String] => List[(String, Int)] = { lines =>
    // println("-->> wordCount")
    lines
      .mkString
      .toLowerCase
      .split("\\W+")
      .toList
      .map(_.filter(c => c.isLetter))
      .filter(_.length > 3)
      .groupBy(s => s)
      // .mapValues(_.length) // deprecated in 2.13
      .view
      .mapValues(_.length)  // gives us a MapView[K, V] // Scala 2.13
      .toList
      .filter(_._2 > limit) // return only words with occurences > limit
      .sortWith(_._2 > _._2)
  }

  def using[A, CL <: { def close(): Unit }](closeable: CL)(f: CL => A): A =
    try {
      f(closeable)
    } finally {
      closeable.close()
    }

  def linesFromUrl(url: URL): List[String] =
    using {
      Source.fromURL(url)
    } {
      _.getLines().toList
    }
}
