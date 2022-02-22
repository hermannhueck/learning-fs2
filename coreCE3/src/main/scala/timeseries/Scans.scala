package timeseries

import fs2.{Chunk, Scan, Stream}
import fs2.timeseries.{TimeSeries, TimeStamped}
import cats.effect.{Ref, Temporal}
import scodec.bits.ByteVector
import scala.collection.immutable.Queue
import scala.concurrent.duration._

// /** Wrapper that associates a time with a value. */
// case class TimeStamped[+A](time: FiniteDuration, value: A) {
//   def map[B](f: A => B): TimeStamped[B] = copy(value = f(value))
//   def mapTime(f: FiniteDuration => FiniteDuration): TimeStamped[A] = copy(time = f(time))
// }

object Scans {

  def bitrate =
    TimeStamped.withPerSecondRate[Option[ByteVector], Long](_.map(_.size).getOrElse(0L) * 8)

  def averageBitrate =
    bitrate.andThen(Scan.stateful1(Queue.empty[Long]) {
      case (q, tsv @ TimeStamped(_, Right(_))) => (q, tsv)
      case (q, TimeStamped(t, Left(sample)))   =>
        val q2      = (sample +: q).take(10)
        val average = q2.sum / q2.size
        (q, TimeStamped(t, Left(average)))
    })

  def measureAverageBitrate[F[_]: Temporal](store: Ref[F, Long], input: Stream[F, Byte]): Stream[F, Byte] =
    TimeSeries
      .timePulled(input.chunks.map(_.toByteVector), 1.second, 1.second)
      .through(averageBitrate.toPipe)
      .flatMap {
        case TimeStamped(_, Left(bitrate))      => Stream.exec(store.set(bitrate))
        case TimeStamped(_, Right(Some(bytes))) => Stream.chunk(Chunk.byteVector(bytes))
        case TimeStamped(_, Right(None))        => Stream.empty
      }
}
