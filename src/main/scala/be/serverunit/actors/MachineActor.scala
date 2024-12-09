package be.serverunit.actors

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import be.serverunit.database.*
import be.serverunit.database.operations.ComplexInsert.*
import slick.jdbc.JdbcBackend.Database

import java.time.Instant
import scala.concurrent.ExecutionContextExecutor
import scala.language.postfixOps
import scala.util.{Failure, Success}

class MachineActor(machineID: Int, db: Database) {
  def initialBehavior(): Behavior[MachineActor.MachineMessage] = Behaviors.setup { context =>
    var currentSet: Option[Set] = None

    implicit val ec: ExecutionContextExecutor = context.executionContext
    val log = context.log

    Behaviors.receiveMessage {
      case MachineActor.StartData(user, receivedTime, weight) =>
        insertStartData(db, machineID, user, receivedTime, weight).onComplete {
          case Success(Some(s)) =>
            currentSet = Some(s)
          case Failure(e) =>
            log.error("Error: {}", e)
          case _ =>
            log.error("Error: No set found")
        }
        Behaviors.same

      case MachineActor.Data(distance, timer) =>
        currentSet match {
          case Some(s: Set) =>
            insertData(db, s, distance, timer)
            Behaviors.same
          case None =>
            log.error("Error: No set found")
            Behaviors.same
        }

      case MachineActor.EndData(reps, time) =>
        currentSet match {
          case Some(s: Set) =>
            insertEndData(db, s, reps, time)
            Behaviors.stopped
          case None =>
            log.error("Error: No set found")
            Behaviors.stopped
        }
    }
  }
}

object MachineActor {

  def apply(machineID: Int, db: Database): Behavior[MachineMessage] =
    new MachineActor(machineID, db).initialBehavior()

  sealed trait MachineMessage

  case class StartData(user: String, receivedTime: Instant, weight: Float) extends MachineMessage

  case class Data(distance: Int, timer: Float) extends MachineMessage

  case class EndData(reps: Int, time: Instant) extends MachineMessage
}