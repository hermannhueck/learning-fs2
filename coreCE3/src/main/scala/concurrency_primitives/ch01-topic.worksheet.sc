// see: https://fs2.io/#/concurrency-primitives - Topic

import cats.effect._
import cats.effect.unsafe.implicits.global
import fs2.Stream
import fs2.concurrent.Topic

Topic[IO, String]
  .flatMap { topic =>
    val publisher  = Stream.constant("1").covary[IO].through(topic.publish)
    val subscriber = topic.subscribe(10).take(4)
    subscriber.concurrently(publisher).compile.toVector
  }
  .unsafeRunSync()
