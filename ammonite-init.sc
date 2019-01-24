import fs2._, cats.effect._, cats.effect.implicits._, cats.implicits._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.higherKinds
implicit val contextShiftIO: ContextShift[IO] = IO.contextShift(global)
implicit val timerIO: Timer[IO] = IO.timer(global)

println("\nAmmonnite initilized!\n")
