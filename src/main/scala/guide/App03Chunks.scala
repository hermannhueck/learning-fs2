package guide

import cats.effect.IO
import fs2.{Chunk, Stream}

object App03Chunks extends App {

  println("\n-----")

  val s1c = Stream.chunk(Chunk.doubles(Array(1.0, 2.0, 3.0)))
   // s1c: fs2.Stream[[x]fs2.Pure[x],Double] = Stream(..)

  s1c.mapChunks { ds =>
    val doubles = ds.toDoubles
    /* do things unboxed using doubles.{values,size} */
    doubles
  }
  // res17: fs2.Stream[[x]fs2.Pure[x],Double] = Stream(..)

  println("-----\n")
}
