package be.serverunit.database.operations

import be.serverunit.database.operations.Basic.*
import be.serverunit.database.utils.PrintDB
import be.serverunit.database.{Repetition, Set}
import slick.jdbc.JdbcBackend.Database

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.postfixOps
import scala.util.{Failure, Success}

object ComplexInsert {

  def insertStartData(db: Database, machineID: Int, userID: String, time: Instant, weight: Float): Future[Option[Set]] = {
    getLastSessionByUser(db, userID).flatMap {
      case Some(sess) =>
        val newSet = Set(0, sess.id, machineID, time, None, None, weight)
        // Insert the set and retrieve the SET
        insertSetWithReturn(db, newSet).map(insertedSet => Some(insertedSet))
      case None =>
        Future.failed(new RuntimeException("No session found for the user"))
    }
  }

  def insertData(db: Database, currentSet: Set, distance: Int, timer: Float): Unit = {
    val newRepetition = Repetition(currentSet.id, timer, distance)
    insertRepetition(db, newRepetition).onComplete({
      case Success(_) => println("Repetition inserted")
        PrintDB.printDatabaseContents(db)
      case Failure(e) => println(s"Error: $e")
    }
    )
  }

  def insertEndData(db: Database, currentSet: Set, reps: Int, time: Instant): Unit = {
    val updatedSet = currentSet.copy(repetitions = Some(reps), endDate = Some(time))
    updateSet(db, updatedSet).onComplete({
      case Success(_) => println("Set updated")
        PrintDB.printDatabaseContents(db)
      case Failure(e) => println(s"Error: $e")
    }
    )
  }
}
