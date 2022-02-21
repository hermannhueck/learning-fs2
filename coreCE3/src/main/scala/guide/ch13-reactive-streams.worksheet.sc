// see: https://fs2.io/#/guide - Reactive streams

import fs2._
import fs2.interop.reactivestreams._
import cats.effect.{IO, Resource}

val stream = Stream(1, 2, 3).covary[IO]
// stream: Stream[IO, Int] = Stream(..)
stream.toUnicastPublisher
// res55: Resource[IO[A], StreamUnicastPublisher[IO[A], Int]] = Bind(
//   source = Bind(
//     source = Bind(
//       source = Allocate(
//         resource = cats.effect.kernel.Resource$$$Lambda$7512/0x00000008022319c8@25b981c6
//       ),
//       fs = cats.effect.kernel.Resource$$Lambda$8062/0x000000080230b608@2c877885
//     ),
//     fs = cats.effect.std.Dispatcher$$$Lambda$8063/0x000000080230b9d8@78ff8845
//   ),
//   fs = cats.effect.kernel.Resource$$Lambda$8062/0x000000080230b608@1fa0d7b6
// )

val publisher: Resource[IO, StreamUnicastPublisher[IO, Int]] = Stream(1, 2, 3).covary[IO].toUnicastPublisher
// publisher: Resource[IO, StreamUnicastPublisher[IO, Int]] = Bind(
//   source = Bind(
//     source = Bind(
//       source = Allocate(
//         resource = cats.effect.kernel.Resource$$$Lambda$7512/0x00000008022319c8@6593deff
//       ),
//       fs = cats.effect.kernel.Resource$$Lambda$8062/0x000000080230b608@7c17b89c
//     ),
//     fs = cats.effect.std.Dispatcher$$$Lambda$8063/0x000000080230b9d8@7c78b241
//   ),
//   fs = cats.effect.kernel.Resource$$Lambda$8062/0x000000080230b608@4b9cd956
// )
publisher.use { p =>
  p.toStreamBuffered[IO](1).compile.toList
}
// res56: IO[List[Int]] = Uncancelable(
//   body = cats.effect.IO$$$Lambda$7517/0x0000000802233158@48c251e1,
//   event = cats.effect.tracing.TracingEvent$StackTrace
// )
