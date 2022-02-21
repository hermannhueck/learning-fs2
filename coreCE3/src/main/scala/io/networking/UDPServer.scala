package io.networking

import cats.effect.{Concurrent, IO, IOApp}
import com.comcast.ip4s._
import fs2.Stream
import fs2.io.net.Network

object UDPServer extends IOApp.Simple {

  def echoServer[F[_]: Concurrent: Network]: Stream[F, Nothing] =
    Stream.resource(Network[F].openDatagramSocket(port = Some(port"5555"))).flatMap { socket =>
      socket.reads.through(socket.writes)
    }

  def run: IO[Unit] =
    echoServer[IO].compile.drain
}
