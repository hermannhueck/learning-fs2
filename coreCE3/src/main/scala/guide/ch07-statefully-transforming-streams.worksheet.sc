// see: https://fs2.io/#/guide - Statefully Transforming Streams

import fs2._

//----- using Stream#scanChunksOpt(n: Long)

// type Pipe[F[_], -I, +O] = (Stream[F, I]) => Stream[F, O]
def take_1[F[_], O](n: Long): Pipe[F, O, O] =
  in =>
    in.scanChunksOpt(n) { n =>
      if (n <= 0) None
      else
        Some(chunk =>
          chunk.size match {
            case size if size < n => (n - size, chunk)
            case _                => (0, chunk.take(n.toInt))
          }
        )
    }

Stream(1, 2, 3, 4).through(take_1(2)).toList
// res32: List[Int] = List(1, 2)

val p1 = Pull.output1(1)
// p1: Pull[Nothing, Int, Unit] = Output(values = Chunk(1))
val s1 = p1.stream
// s1: Stream[Nothing, Int] = Stream(..)
p1 >> Pull.output1(2)
// res34: Pull[Nothing, Int, Unit] = <function1>

s1.pull.echo
// res35: Pull[Nothing, Int, Unit] = InScope(
//   stream = Output(values = Chunk(1)),
//   useInterruption = false
// )
s1.pull.uncons
// res36: Pull[Nothing, Nothing, Option[(Chunk[Int], Stream[Nothing, Int])]] = <function1>

//----- using Stream#pull#uncons

def take_2[F[_], O](n: Long): Pipe[F, O, O] = {
  def go(s: Stream[F, O], n: Long): Pull[F, O, Unit] = {
    s.pull.uncons.flatMap {
      case Some((head, tail)) =>
        head.size match {
          case size if size <= n => Pull.output(head) >> go(tail, n - size)
          case _                 => Pull.output(head.take(n.toInt))
        }
      case None               => Pull.done
    }
  }
  in => go(in, n).stream
}

Stream(1, 2, 3, 4).through(take_2(2)).toList
// res38: List[Int] = List(1, 2)

//----- using Stream#pull#take(n: Long)

def take_3[F[_], O](n: Long): Pipe[F, O, O] = { in =>
  in.pull.take(n).void.stream
}

Stream(1, 2, 3, 4).through(take_3(2)).toList
// res41: List[Int] = List(1, 2)
