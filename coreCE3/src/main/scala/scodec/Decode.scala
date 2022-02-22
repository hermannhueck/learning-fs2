import cats.effect.{IO, IOApp}
import scodec.bits._
import scodec.codecs._
import fs2.Stream
import fs2.interop.scodec._
import fs2.io.file.{Files, Path}

object Decode extends IOApp.Simple {

  def run = {
    val frames: StreamDecoder[ByteVector] =
      StreamDecoder.many(int32).flatMap { numBytes => StreamDecoder.once(bytes(numBytes)) }

    val filePath = Path("largefile.bin")

    val s: Stream[IO, ByteVector] =
      Files[IO].readAll(filePath).through(frames.toPipeByte)

    s.compile.count.flatMap(cnt => IO.println(s"Read $cnt frames."))
  }
}
