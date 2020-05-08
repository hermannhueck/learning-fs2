package guide.ch07resource

import java.io.BufferedInputStream
import java.io.FileInputStream
import java.io.InputStream
import java.util.concurrent.Executors

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContextExecutorService

import cats.effect.Blocker
import cats.effect.ContextShift
import cats.effect.IO
import fs2.Stream
import fs2.text

object App03ExecutionContextResource extends hutil.App {

  val allocateEC: IO[ExecutionContextExecutorService] = IO {
    println("========>>>>> allocating ExecutionContext ...")
    ExecutionContext.fromExecutorService(Executors.newCachedThreadPool)
  }

  val shutdownEC: ExecutionContextExecutorService => IO[Unit] = ec =>
    IO {
      println("========>>>>> shutting down ExecutionContext ...")
      ec.shutdown()
    }

  val ecStream: Stream[IO, ExecutionContextExecutorService] = Stream.bracket(allocateEC)(shutdownEC)

  val openFile: IO[InputStream] = IO {
    println("=====>>>>> opening file ...")
    new BufferedInputStream(new FileInputStream("README.md"))
  }

  val closeFile: InputStream => IO[Unit] = in =>
    IO {
      println("=====>>>>> closing file ...")
      in.close()
    }

  val readerStream: Stream[IO, InputStream] = Stream.bracket(openFile)(closeFile)

  val linesStream = ecStream flatMap { blockingEC =>
    val blocker                       = Blocker.liftExecutionContext(blockingEC)
    implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

    readerStream.flatMap { in: InputStream =>
      fs2
        .io
        .readInputStream(IO(in), 64, blocker)
        .through(text.utf8Decode)
        .through(text.lines) // ++ Stream.eval[IO, Byte](IO(throw new RuntimeException("byte stream error")))
    }
  }

  println("-------------- count lines:")
  val ioCount: IO[Int] = linesStream.compile.fold(0)((sum, _) => sum + 1)
  println(ioCount.unsafeRunSync())
}
