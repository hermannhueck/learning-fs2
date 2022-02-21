// see: https://fs2.io/#/guide - Resource Aquisition

import fs2.Stream
import cats.effect.IO
// import cats.effect.unsafe.implicits.global

val count   = new java.util.concurrent.atomic.AtomicLong(0)
// count: java.util.concurrent.atomic.AtomicLong = 0
val acquire = IO { println("incremented: " + count.incrementAndGet); () }
// acquire: IO[Unit] = IO(...)
val release = IO { println("decremented: " + count.decrementAndGet); () }
// release: IO[Unit] = IO(...)

val err = Stream.raiseError[IO](new Exception("oh noes!"))

// Stream.bracket(acquire)(_ => release).flatMap(_ => Stream(1, 2, 3) ++ err).compile.drain.unsafeRunSync()
// java.lang.Exception: oh noes!
//     at repl.MdocSession$App.<init>(guide.md:149)
//     at repl.MdocSession$.app(guide.md:3)

count.get
// res26: Long = 0L
