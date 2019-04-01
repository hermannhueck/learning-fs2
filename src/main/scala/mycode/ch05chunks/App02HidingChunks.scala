package mycode.ch05Chunks

import cats.effect.IO
import fs2.{Chunk, Stream}

object App01Chunks extends App {

  println("\n----- Chunks")

  def printAsList[A](s: Stream[IO, A]): Unit = println(s.compile.toList.unsafeRunSync())

  val stream: Stream[IO, Int] = Stream.range(0, 16).covary[IO]

  println("\n// stream: Stream[IO, Int]")
  printAsList(stream)

  println("\n// stream.chunks: Stream[IO, Chunk[Int]]")
  printAsList(stream.chunks)

  println("\n// stream.chunkN(4): Stream[IO, Chunk[Int]]")
  printAsList(stream.chunkN(4))

  println("\n// stream.chunkLimit(4): Stream[IO, Chunk[Int]]")
  printAsList(stream.chunkLimit(4))

  println("\n// unchunkedReversed: Stream[IO, Chunk[Int]]")
  val unchunkedReversed = unchunkReversed(stream.chunkN(4))
  printAsList(unchunkedReversed)

  println("\n// unchunkedReversed.chunks: Stream[IO, Int]")
  printAsList(unchunkedReversed.chunks)

  println("\n// unchunked: Stream[IO, Chunk[Int]]")
  val unchunked = unchunk(stream.chunkN(4))
  printAsList(unchunked)

  println("\n// unchunked.chunks: Stream[IO, Int]")
  printAsList(unchunked.chunks)


  def unchunkReversed[F[_], O](chunkStream: Stream[F, Chunk[O]]): Stream[F, O] = {
    val empty: Stream[F, O] = Stream.empty.covary[F]
    chunkStream.fold(empty)((stream, chunk) => stream cons chunk).flatten // cons reverses the Stream of Chunks
  }

  def unchunk[F[_], O](chunkStream: Stream[F, Chunk[O]]): Stream[F, O] = {
    val empty: Stream[F, O] = Stream.empty.covary[F]
    chunkStream.fold(empty)((stream, chunk) => stream ++ Stream.chunk(chunk)).flatten // ++ maintains the order of Chunks
  }

  println("-----\n")
}
