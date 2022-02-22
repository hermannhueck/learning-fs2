package timeseries

import fs2.Stream
import scodec.bits.ByteVector
import scala.concurrent.duration._
import cats.effect.Temporal
import fs2.timeseries.{TimeSeries, TimeStamped}

// /** Wrapper that associates a time with a value. */
// case class TimeStamped[+A](time: FiniteDuration, value: A) {
//   def map[B](f: A => B): TimeStamped[B] = copy(value = f(value))
//   def mapTime(f: FiniteDuration => FiniteDuration): TimeStamped[A] = copy(time = f(time))
// }

object Timeseries2 {

  def withBitrate[F[_]](
      input: Stream[F, TimeStamped[Option[ByteVector]]]
  ): Stream[F, TimeStamped[Either[Long, Option[ByteVector]]]] =
    TimeStamped.withPerSecondRate[Option[ByteVector], Long](_.map(_.size).getOrElse(0L) * 8).toPipe(input)

  def withReceivedBitrate[F[_]: Temporal](
      input: Stream[F, Byte]
  ): Stream[F, TimeStamped[Either[Long, Option[ByteVector]]]] =
    TimeSeries.timePulled(input.chunks.map(_.toByteVector), 1.second, 1.second).through(withBitrate)
}
