package be.serverunit.actors

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import be.serverunit.database.*
import be.serverunit.database.BasicOperations.{getLastSessionByUser, getSessionByUser}
import be.serverunit.database.Operations.*

import java.time.LocalDateTime
import scala.concurrent.ExecutionContextExecutor
import scala.language.postfixOps
import slick.jdbc.JdbcBackend.Database

import scala.util.{Failure, Success}

object MachineActor{

  sealed trait MachineMessage
  case class StartData(user: String, receivedTime: LocalDateTime, weight: Int) extends MachineMessage
  case class Data(distance: Int, timer: Int) extends MachineMessage
  case class EndData(reps: Int, time: LocalDateTime) extends MachineMessage

  def apply(machineID: Int, db: Database): Behavior[MachineMessage] = Behaviors.setup(context =>
    var currentSet: Option[Set] = None
    
    implicit val ec: ExecutionContextExecutor = context.executionContext
  
    Behaviors.receiveMessage {
      case StartData(user, receivedTime, weight) =>

        insertStartData(db, machineID, user, receivedTime, weight).onComplete {
          case Success(Some(s)) =>
            currentSet = Some(s)
          case Failure(e) =>
            println(s"Error: $e")
        }
        
        Behaviors.same

      case Data(distance, timer) =>
        // Inserting the data into the database
        currentSet match {
          case Some(s: Set) =>
            insertData(db, s, distance, timer)
            Behaviors.same
          case None =>
            println("Error: No set found")
            Behaviors.same
        }
  
  
      case EndData(reps, time) =>
        // Inserting the data into the database and stop the actor afterwards
        currentSet match {
          case Some(s: Set) =>
            insertEndData(db, s, reps, time)
            Behaviors.stopped
          case None =>
            println("Error: No set found")
            Behaviors.stopped
        }

    }
  )
}
