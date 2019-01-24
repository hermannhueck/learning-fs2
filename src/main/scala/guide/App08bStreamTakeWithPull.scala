package guide

import fs2.{Pipe, Pull, Stream}

import scala.language.higherKinds

object App08bStreamTakeWithPull extends App {

  println("\n-----")

  def take[F[_], O](n: Long): Pipe[F, O, O] = {

    def go(s: Stream[F, O], toTake: Long): Pull[F, O, Unit] = {
      s.pull.uncons.flatMap {
        case Some((head, tail)) =>
          head.size match {
            case size if size <= toTake => Pull.output(head) >> go(tail, toTake - size)
            case _ => Pull.output(head.take(toTake.toInt)) >> Pull.done
          }
        case None => Pull.done
      }
    }

    in => go(in, n).stream
  }
  // take: [F[_], O](n: Long)fs2.Pipe[F,O,O]

  val myRes = Stream(1,2,3,4).through(take(2)).toList
  // myRes: List[Int] = List(1, 2)
  println(myRes)

  val res = Stream(1,2,3,4).take(2).toList
  // res: List[Int] = List(1, 2)
  println(res)

  println("-----\n")
}
