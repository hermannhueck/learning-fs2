package mycode.ch05Chunks

import fs2.{Chunk, Stream}

object App03Chunks extends App {

  println("\n-----")

  val source = Stream.chunk(Chunk.doubles(Array(1.0, 2.0, 3.0)))
  // stream: fs2.Stream[[x]fs2.Pure[x],Double] = Stream(..)
  println(source.toList)

  val mapped = source.mapChunks { ds =>
    val doubles: Chunk.Doubles = ds.toDoubles
    /* do things unboxed (directly on the Array[Double] using doubles.{values,size} */
    val values: Array[Double] = doubles.values
    def square(x: Double): Double = x * x
    val mapped: Array[Double] = values.map(square)
    Chunk.doubles(mapped)
  }
  // mapped: fs2.Stream[[x]fs2.Pure[x],Double] = Stream(..)
  println(mapped.toList)

  println("-----\n")
}
