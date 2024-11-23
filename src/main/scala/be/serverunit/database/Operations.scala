package be.serverunit.database

import be.serverunit.database.BasicOperations.*

import java.time.LocalDateTime
import scala.concurrent.Await
import scala.concurrent.duration.*
import scala.language.postfixOps

object Operations {
  def insertStartData(currentSession: Session, machineID: Int, userID: String, time: LocalDateTime, weight: Int): Option[Set] = {
    val latestSet = Await.result(getLastSetBySessionAndMachine(currentSession.id, machineID), 1 seconds)
    
    val newSet = latestSet match {
        case Some(set) =>
          Set(set.id + 1, currentSession.id, machineID, time, None, None, weight)
        case None =>
          Set(0, currentSession.id, machineID, time, None, None, weight)
      }
    insertSet(newSet)
    Some(newSet)
  }
  
  def insertData(currentSet: Set, distance: Int, timer: Int): Unit = {
    val newRepetition = Repetition(0, currentSet.id, timer, distance)
    insertRepetition(newRepetition)
  }
  
  def insertEndData(currentSet: Set, reps: Int, time: LocalDateTime): Unit = {
  }
}
