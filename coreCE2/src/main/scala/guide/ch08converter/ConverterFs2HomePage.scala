package guide.ch08converter

import java.nio.file.{Path, Paths}

import cats.effect.{Blocker, ExitCode, IO}
import fs2.{io, text, Stream}

/*
  Step-by-step explanation at:
  https://github.com/functional-streams-for-scala/fs2/blob/series/1.0/docs/src/ReadmeExample.md
  and at:
  https://www.youtube.com/watch?v=cahvyadYfX8
 */
object ConverterFs2HomePage extends hutil.IOApp {

  val input: Path  = Paths.get("testdata/fahrenheit.txt")
  val output: Path = Paths.get("testdata/celsius.txt")

  def fahrenheitToCelsius(f: Double): Double =
    (f - 32.0) * (5.0 / 9.0)

  val converter: Stream[IO, Unit] = Stream.resource(Blocker[IO]).flatMap { blocker =>
    io.file
      .readAll[IO](input, blocker, 4096)
      .through(text.utf8Decode)
      .through(text.lines)
      .filter(s => !s.trim.isEmpty && !s.startsWith("//"))
      .map(line => fahrenheitToCelsius(line.toDouble).toString)
      .intersperse("\n")
      .through(text.utf8Encode)
      .through(io.file.writeAll(output, blocker))
  }

  def ioRun(args: List[String]): IO[ExitCode] =
    converter.compile.drain.as(ExitCode.Success)
}
