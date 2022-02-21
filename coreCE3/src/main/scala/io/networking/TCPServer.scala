package io.networking

import cats.effect.{IO, IOApp}
import com.comcast.ip4s._
import fs2.io.net.{Network, Socket}
import fs2.{Stream, text}

object TCPServer extends IOApp.Simple {

  def handleClient[F[_]](client: Socket[F]): Stream[F, Unit] =
    client
      .reads
      .through(text.utf8.decode)
      .through(text.lines)
      .interleave(Stream.constant("\n"))
      .through(text.utf8.encode)
      .through(client.writes)
      .handleErrorWith(_ => Stream.empty) // handle errors of client sockets

  def echoServer[F[_]: Network]: Stream[F, Stream[F, Unit]] =
    Network[F].server(port = Some(port"5555")).map(handleClient)

  def echoServer2[F[_]: Network]: Stream[F, Stream[F, Unit]] =
    Network[F].server(port = Some(port"5555")).map(client => client.reads.through(client.writes))

  val maxConcurrentClients = 100

  def run: IO[Unit] =
    echoServer2[IO].parJoin(maxConcurrentClients).compile.drain
}
