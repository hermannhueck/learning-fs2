// see: https://fs2.io/#/guide - Error Handling

import fs2.Stream
import cats.effect.IO
import cats.effect.unsafe.implicits.global

val err  = Stream.raiseError[IO](new Exception("oh noes!"))
// err: Stream[IO, Nothing] = Stream(..)
val err2 = Stream(1, 2, 3) ++ (throw new Exception("!@#$"))
// err2: Stream[Nothing, Int] = Stream(..)
val err3 = Stream.eval(IO(throw new Exception("error in effect!!!")))
// err3: Stream[IO, Nothing] = Stream(..)

try err.compile.toList.unsafeRunSync()
catch { case e: Exception => println(e) }
// java.lang.Exception: oh noes!
// res21: Any = ()

try err2.toList
catch { case e: Exception => println(e) }
// java.lang.Exception: !@#$
// res22: Any = ()

try err3.compile.drain.unsafeRunSync()
catch { case e: Exception => println(e) }
// java.lang.Exception: error in effect!!!

err
  .handleErrorWith { e =>
    Stream.emit(e.getMessage)
  }
  .compile
  .toList
  .unsafeRunSync()
// res24: List[String] = List("oh noes!")

val err4 = Stream(1, 2, 3).covary[IO] ++
  Stream.raiseError[IO](new Exception("bad things!")) ++
  Stream.eval(IO(4))
// err4: Stream[IO[x], Int] = Stream(..)

err4.handleErrorWith { _ => Stream(0) }.compile.toList.unsafeRunSync()
// res25: List[Int] = List(1, 2, 3, 0)
