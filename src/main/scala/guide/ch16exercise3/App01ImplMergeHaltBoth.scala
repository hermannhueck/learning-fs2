package guide.ch16exercise3

import cats.effect.{Concurrent, ContextShift, IO}
import fs2.{Chunk, Pipe, Pipe2, Stream}
import scala.concurrent.ExecutionContext
import scala.util.chaining._
import munit.Assertions._

object App01ImplMergeHaltBoth extends hutil.App {

  // type Pipe2[F[_], -I, -I2, +O] = (Stream[F, I], Stream[F, I2]) => Stream[F, O]

  /** Like `merge`, but halts as soon as _either_ branch halts. */
  @scala.annotation.nowarn("cat=unused-params")
  def mergeHaltBoth[F[_]: Concurrent, O]: Pipe2[F, O, O, O] = { (s1, s2) =>
    /*
      How to implement ???
      ????????????????????????????????????????
     */
    Stream(s1, s2).parJoinUnbounded // corresponds to merge
    ???
  }

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  val s1 = Stream(1, 2, 3).covary[IO]
  val s2 = Stream.eval(IO { Thread.sleep(200); 4 })
  val s3 = Stream(4).covary[IO]

  // The merge function runs two streams concurrently, combining their outputs. It halts when either input has halted:
  val merged = s1 mergeHaltBoth s2

  merged
    .compile
    .toVector
    .unsafeRunSync
    .tap(println)
    .pipe(assertEquals(_, Vector(1, 2, 3)))
  // res0: Vector[Int] = Vector(1, 2, 3)
}
