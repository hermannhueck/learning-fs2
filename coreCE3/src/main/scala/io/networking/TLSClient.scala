package io.networking

import cats.effect.std.Console
import cats.effect.{IO, IOApp, MonadCancelThrow}
import com.comcast.ip4s._
import fs2.io.net.Network
import fs2.io.net.tls.TLSContext
import fs2.{Stream, text}

object TLSClient extends IOApp.Simple {

  def client1[F[_]: MonadCancelThrow: Console: Network](tlsContext: TLSContext[F]): Stream[F, Unit] = {
    Stream.resource(Network[F].client(SocketAddress(host"localhost", port"5555"))).flatMap { underlyingSocket =>
      Stream.resource(tlsContext.client(underlyingSocket)).flatMap { socket =>
        Stream("Hello, world!")
          .interleave(Stream.constant("\n"))
          .through(text.utf8.encode)
          .through(socket.writes) ++
          socket
            .reads
            .through(text.utf8.decode)
            .through(text.lines)
            .head
            .foreach { response =>
              Console[F].println(s"Response: $response")
            }
      }
    }
  }

  def run: IO[Unit] =
    Network[IO].tlsContext.system.flatMap { tlsContext =>
      client1[IO](tlsContext).compile.drain
    }
}
