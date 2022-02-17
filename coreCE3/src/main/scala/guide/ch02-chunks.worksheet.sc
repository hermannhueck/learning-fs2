// see: https://fs2.io/#/guide - Chunks

import fs2.Stream
import fs2.Chunk

val s1c = Stream.chunk(Chunk.array(Array(1.0, 2.0, 3.0)))
// s1c: Stream[Nothing, Double] = Stream(..)
