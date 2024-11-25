package be.serverunit.database

import be.serverunit.database.BasicOperations.*
import slick.jdbc.JdbcBackend.Database

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.postfixOps

object Operations {

  def insertStartData(db: Database, machineID: Int, userID: String, time: LocalDateTime, weight: Int): Future[Option[Set]] = {
    getLastSessionByUser(db, userID).flatMap {
      case Some(sess) =>
        val newSet = Set(0, sess.id, machineID, time, None, None, weight)
        insertSet(db, newSet).map(_ => Some(newSet))
      case None =>
        Future.failed(new RuntimeException("No session found for the user"))
    }
  }

  def insertData(db: Database, currentSet: Set, distance: Int, timer: Int): Unit = {
    val newRepetition = Repetition(0, currentSet.id, currentSet.sessionID, currentSet.machineID, timer, distance)
    insertRepetition(db, newRepetition)
  }

  def insertEndData(db: Database, currentSet: Set, reps: Int, time: LocalDateTime): Unit = {
    val updatedSet = currentSet.copy(repetitions = Some(reps), endDate = Some(time))
    updateSet(db, updatedSet)
  }
}
