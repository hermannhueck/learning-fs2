import fs2._, cats.effect._, cats.effect.implicits._, cats.implicits._
println("\nimport fs2._, cats.effect._, cats.effect.implicits._, cats.implicits._")
import scala.concurrent.ExecutionContext.Implicits.global
println("import scala.concurrent.ExecutionContext.Implicits.global")
import scala.concurrent.duration._
println("import scala.concurrent.duration._")
import scala.language.higherKinds
println("import scala.language.higherKinds")
implicit val contextShiftIO: ContextShift[IO] = IO.contextShift(global)
println("implicit val contextShiftIO: ContextShift[IO] = IO.contextShift(global)")
implicit val timerIO: Timer[IO] = IO.timer(global)
println("implicit val timerIO: Timer[IO] = IO.timer(global)")

println("\nAmmonnite session initialized!\n")
