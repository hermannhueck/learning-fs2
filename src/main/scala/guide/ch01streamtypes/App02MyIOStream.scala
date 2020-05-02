package guide.ch01streamtypes

import fs2.Stream
import myio.MyIO

object App02MyIOStream extends hutil.App {

  println("----- Stream of MyIO effects")

  val stream: Stream[MyIO, Int] =
    Stream
      .eval(MyIO.eval {
        println("BEING RUN!!"); 1 + 1
      })
      .repeat
      .take(3)

  val effect: MyIO[Vector[Int]] = stream.compile.toVector // Up to this point nothing is run

  val result: Vector[Int] = effect.unsafeRunSync() // produces side effects (println) and returns the result

  println(result)
}
