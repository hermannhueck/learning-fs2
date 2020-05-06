package guide.ch07resource

import java.io.{BufferedInputStream, FileInputStream, InputStream}
import java.util.concurrent.Executors

import cats.effect.{ContextShift, IO, Resource}
import fs2.{text, Stream}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService}
import cats.effect.Blocker

object App04ResourceMake extends hutil.App {

  val allocateEC: IO[ExecutionContextExecutorService] = IO {
    println("========>>>>> allocating ExecutionContext ...")
    ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(2))
  }

  val shutdownEC: ExecutionContextExecutorService => IO[Unit] = ec =>
    IO {
      println("========>>>>> releasing ExecutionContext ...")
      ec.shutdown()
    }

  val ecResource: Resource[IO, ExecutionContextExecutorService] = Resource.make(allocateEC)(shutdownEC)
  val ecStream: Stream[IO, ExecutionContext]                    = Stream.resource(ecResource)

  val openFile: IO[InputStream] = IO {
    println("=====>>>>> opening file ...")
    new BufferedInputStream(new FileInputStream("README.md"))
  }

  val closeFile: InputStream => IO[Unit] = in =>
    IO {
      println("=====>>>>> closing file ...")
      in.close()
    }

  val inputStreamResource: Resource[IO, InputStream] = Resource.make(openFile)(closeFile)
  val readerStream: Stream[IO, InputStream]          = Stream.resource(inputStreamResource)

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
