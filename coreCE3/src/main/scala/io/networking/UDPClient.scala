package io.networking

import cats.effect.std.Console
import cats.effect.{Concurrent, IO, IOApp}
import com.comcast.ip4s._
import fs2.io.net.{Datagram, Network}
import fs2.{Stream, text}

object UDPClient extends IOApp.Simple {

  def client[F[_]: Concurrent: Console: Network]: Stream[F, Nothing] = {
    val address = SocketAddress(ip"127.0.0.1", port"5555")
    Stream.resource(Network[F].openDatagramSocket()).flatMap { socket =>
      Stream("Hello, world!")
        .through(text.utf8.encode)
        .chunks
        .map(data => Datagram(address, data))
        .through(socket.writes)
        .drain ++
        socket
          .reads
          .flatMap(datagram => Stream.chunk(datagram.bytes))
          .through(text.utf8.decode)
          .foreach { response =>
            Console[F].println(s"Response: $response")
          }
    }
  }

  def run: IO[Unit] =
    client[IO].compile.drain
}
