package mycode.ch06errors

import cats.syntax.applicativeError._
import cats.effect.IO
import fs2.{INothing, Pure, Stream}

object App05ErrorHandling extends App {

  println("\n-----")

  // A stream can raise errors, either explicitly, using Stream.raiseError, or implicitly via an exception in pure code or inside an effect passed to eval:

  val err1: Stream[IO, INothing] = Stream.raiseError[IO](new Exception("oh noes!"))
  // err: fs2.Stream[cats.effect.IO,fs2.INothing] = Stream(..)

  val err2: Stream[Pure, Int] = Stream(1,2,3) ++ (throw new Exception("!@#$"))
  // err2: fs2.Stream[[x]fs2.Pure[x],Int] = Stream(..)

  val err3: Stream[IO, Nothing] = Stream.eval(IO(throw new Exception("error in effect!!!")))
  // err3: fs2.Stream[cats.effect.IO,Nothing] = Stream(..)

  // All these fail when running:

  val res0 = try err1.compile.toList.unsafeRunSync catch { case e: Exception => println(e) }
  //=> java.lang.Exception: oh noes!
  //res0: Any = ()

  val res1 = try err2.toList catch { case e: Exception => println(e) }
  //=> java.lang.Exception: !@#$
  // res1: Any = ()

  val res2 = try err3.compile.drain.unsafeRunSync() catch { case e: Exception => println(e) }
  //=> java.lang.Exception: error in effect!!!

  // The handleErrorWith method lets us catch any of these errors:

  err1.handleErrorWith { e => Stream.emit(e.getMessage) }.compile.toList.unsafeRunSync()
  // res25: List[String] = List(oh noes!)

  err2.covary[IO].handleError { _ => -1 }

  println("-----\n")
}
