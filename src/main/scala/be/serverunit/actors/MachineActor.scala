package be.serverunit.actors

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import be.serverunit.database.*
import be.serverunit.database.BasicOperations.{getLastSessionByUser, getSessionByUser}
import be.serverunit.database.Operations._

import java.time.LocalDateTime
import scala.concurrent.Await
import scala.concurrent.duration.*
import scala.language.postfixOps

object MachineActor{

  sealed trait MachineMessage
  case class StartData(user: String, receivedTime: LocalDateTime, weight: Int) extends MachineMessage
  case class Data(distance: Int, timer: Int) extends MachineMessage
  case class EndData(reps: Int, time: LocalDateTime) extends MachineMessage

  def apply(machineID: Int): Behavior[MachineMessage] = Behaviors.setup(context =>
    // Variables to store the current session and set
    var currentSet: Option[Set] = None
  
    Behaviors.receiveMessage {
      case StartData(user, receivedTime, weight) =>
        // Get latest session of the user
        val session = Await.result(getLastSessionByUser(user), 1 seconds)
        
        session match {
          case Some(s : Session) =>
            // Insert the start data into the database
            currentSet = insertStartData(s, machineID, user, receivedTime, weight)
            Behaviors.same
          case None =>
            println("Error: No session found")
            Behaviors.same
        }


      case Data(distance, timer) =>
        // Inserting the data into the database
        currentSet match {
          case Some(s: Set) =>
            insertData(s, distance, timer)
            Behaviors.same
          case None =>
            println("Error: No set found")
            Behaviors.same
        }
  
  
      case EndData(reps, time) =>
        // Inserting the data into the database and stop the actor afterwards
        currentSet match {
          case Some(s: Set) =>
            insertEndData(s, reps, time)
            Behaviors.stopped
          case None =>
            println("Error: No set found")
            Behaviors.stopped
        }
        
    }
  )
}
