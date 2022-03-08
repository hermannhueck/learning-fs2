// See: https://www.youtube.com/watch?v=FWYXqYQWAc0
// Tutorial 2 - Sharding a stream of values using Fs2
//
package sharding

import cats.effect._
import cats.effect.std.Queue
import fs2._

object ShardingTutorial3 extends IOApp.Simple {

  val showShardAndValue: Int => Int => IO[Unit] =
    shard => value => IO.println(s"Shard $shard: Value: $value")

  def queueMap(size: Int): IO[Map[Int, Queue[IO, Int]]] =
    Queue
      .bounded[IO, Int](100)
      .replicateA(size)
      .map(_.zipWithIndex.map(_.swap).toMap)

  @annotation.nowarn("cat=unused-params")
  def sharded(source: Stream[IO, Int], shards: Int, action: Int => Int => IO[Unit]): Stream[IO, Unit] =
    for {
      shardMap <- Stream.eval(queueMap(shards))
      value    <- source.covary[IO]
      q         = shardMap(value % shards)
      -        <- Stream.eval(q.offer(value)) concurrently
                    Stream.eval(q.take).evalMap(action(value % shards))
    } yield ()

  val src = Stream.range(0, 10).map(_ + 1)

  val run: IO[Unit] =
    sharded(src, 3, showShardAndValue).compile.drain
}
