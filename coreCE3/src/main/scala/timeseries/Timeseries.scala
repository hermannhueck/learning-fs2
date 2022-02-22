package timeseries

import fs2.Stream
import fs2.timeseries.TimeStamped
import scodec.bits.ByteVector
import cats.effect.Ref
import fs2.Chunk

// /** Wrapper that associates a time with a value. */
// case class TimeStamped[+A](time: FiniteDuration, value: A) {
//   def map[B](f: A => B): TimeStamped[B] = copy(value = f(value))
//   def mapTime(f: FiniteDuration => FiniteDuration): TimeStamped[A] = copy(time = f(time))
// }

object Timeseries {

  def withBitrate[F[_]](input: Stream[F, TimeStamped[ByteVector]]): Stream[F, TimeStamped[Either[Long, ByteVector]]] =
    TimeStamped.withPerSecondRate[ByteVector, Long](_.size * 8).toPipe(input)

  def withReceivedBitrate[F[_]](input: Stream[F, Byte]): Stream[F, TimeStamped[Either[Long, ByteVector]]] =
    input.chunks.map(c => TimeStamped.unsafeNow(c.toByteVector)).through(withBitrate)

  import scala.collection.immutable.Queue

  def withAverageBitrate[F[_]](input: Stream[F, Byte]): Stream[F, TimeStamped[Either[Long, ByteVector]]] =
    withReceivedBitrate(input)
      .mapAccumulate(Queue.empty[Long]) {
        case (q, tsv @ TimeStamped(_, Right(_))) => (q, tsv)
        case (q, TimeStamped(t, Left(sample)))   =>
          val q2      = (sample +: q).take(10)
          val average = q2.sum / q2.size
          (q, TimeStamped(t, Left(average)))
      }
      .map(_._2)

  def measureAverageBitrate[F[_]](store: Ref[F, Long], input: Stream[F, Byte]): Stream[F, Byte] =
    withAverageBitrate(input).flatMap {
      case TimeStamped(_, Left(bitrate)) => Stream.exec(store.set(bitrate))
      case TimeStamped(_, Right(bytes))  => Stream.chunk(Chunk.byteVector(bytes))
    }
}
