package io.networking

import java.net.ConnectException

import scala.concurrent.duration._

import cats.effect.std.Console
import cats.effect.{IO, IOApp, MonadCancelThrow, Temporal}
import cats.syntax.all._
import com.comcast.ip4s._
import fs2.io.net.{Network, Socket}
import fs2.{Chunk, Stream, text}

object TCPClient extends IOApp.Simple {

  def client1[F[_]: MonadCancelThrow: Console: Network]: F[Unit] =
    Network[F].client(SocketAddress(host"localhost", port"5555")).use { socket =>
      socket.write(Chunk.array("Hello, world!".getBytes)) >>
        socket.read(8192).flatMap { response =>
          Console[F].println(s"Response: $response")
        }
    }

  def client2[F[_]: MonadCancelThrow: Console: Network]: Stream[F, Unit] =
    Stream.resource(Network[F].client(SocketAddress(host"localhost", port"5555"))).flatMap { socket =>
      Stream("Hello, world!")
        .through(text.utf8.encode)
        .through(socket.writes) ++
        socket
          .reads
          .through(text.utf8.decode)
          .foreach { response =>
            Console[F].println(s"Response: $response")
          }
    }

  def client3[F[_]: MonadCancelThrow: Console: Network]: Stream[F, Unit] =
    Stream.resource(Network[F].client(SocketAddress(host"localhost", port"5555"))).flatMap { socket =>
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

  def connect[F[_]: Temporal: Network](address: SocketAddress[Host]): Stream[F, Socket[F]] =
    Stream
      .resource(Network[F].client(address))
      .handleErrorWith { case _: ConnectException =>
        connect(address).delayBy(5.seconds)
      }

  def client4[F[_]: Temporal: Console: Network]: Stream[F, Unit] =
    connect(SocketAddress(host"localhost", port"5555")).flatMap { socket =>
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

  def run: IO[Unit] =
    // client1[IO]
    // client2[IO].compile.drain
    // client3[IO].compile.drain
    client4[IO].compile.drain
}
