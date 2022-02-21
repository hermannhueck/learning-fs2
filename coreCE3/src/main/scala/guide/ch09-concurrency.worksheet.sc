// see: https://fs2.io/#/guide - Concurrency

import fs2._
import cats.effect.IO
import cats.effect.unsafe.implicits.global

Stream(1, 2, 3).merge(Stream.eval(IO { Thread.sleep(200); 4 })).compile.toVector.unsafeRunSync()
// res46: Vector[Int] = Vector(1, 2, 3, 4)

// note Concurrent[F] bound
// import cats.effect.Concurrent
// def parJoin[F[_]: Concurrent,O](maxOpen: Int)(outer: Stream[F, Stream[F, O]]): Stream[F, O]
