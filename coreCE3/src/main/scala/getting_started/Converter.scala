package getting_started

import cats.effect.{IO, IOApp}
import fs2.{text, Stream}
import fs2.io.file.{Files, Path}

object Converter extends IOApp.Simple {

  val converter: Stream[IO, Unit] = {
    def fahrenheitToCelsius(f: Double): Double =
      (f - 32.0) * (5.0 / 9.0)

    Files[IO]
      .readAll(Path("../testdata/fahrenheit.txt"))
      .through(text.utf8.decode)
      .through(text.lines)
      .filter(s => !s.trim.isEmpty && !s.startsWith("//"))
      .map(line => fahrenheitToCelsius(line.toDouble).toString)
      .intersperse("\n")
      .through(text.utf8.encode)
      .through(Files[IO].writeAll(Path("../output/celsius.txt")))
  }

  def run: IO[Unit] =
    converter.compile.drain
}
