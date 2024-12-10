package be.serverunit.database.operations

import be.serverunit.database.SlickTables.*
import be.serverunit.database.{Air, Repetition, UserSet, UserSession}
import be.serverunit.traits.database.{AirOperations, RepetitionOperations, SessionOperations, SetOperations}
import slick.jdbc.H2Profile.api.*
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object Basic extends SessionOperations with SetOperations with RepetitionOperations with AirOperations {

  // Session operations
  override def getLastSessionByUser(db: Database, userID: String)(implicit ec: ExecutionContext): Future[Option[UserSession]] = {
    db.run(sessions.filter(_.userID === userID).sortBy(_.id.desc).result.headOption)
  }

  // Set operations
  override def insertSetWithReturn(db: Database, set: UserSet)(implicit ec: ExecutionContext): Future[UserSet] = {
    val insertAction = db.run((sets returning sets.map(_.id)) += set)
    insertAction.onComplete {
      case Success(generatedId) =>
        println(s"Set inserted successfully with ID: $generatedId")
      case Failure(e) =>
        println(s"Failed to insert set: $e")
    }
    insertAction.map(generatedId => set.copy(id = generatedId))
  }

  override def updateASet(db: Database, set: UserSet)(implicit ec: ExecutionContext): Future[Int] = {
    val updateAction = db.run(sets.filter(_.id === set.id).update(set))
    updateAction.onComplete {
      case Success(_) =>
        println(s"Set updated successfully with ID: ${set.id}")
      case Failure(e) =>
        println(s"Failed to update set with ID ${set.id}: $e")
    }
    updateAction
  }

  // Repetition operations
  override def insertRepetition(db: Database, repetition: Repetition)(implicit ec: ExecutionContext): Future[Int] = {
    val insertAction = db.run(repetitions += repetition)
    insertAction.onComplete {
      case Success(_) =>
        println("Repetition inserted successfully")
      case Failure(e) =>
        println(s"Failed to insert repetition: $e")
    }
    insertAction
  }

  // Air operations
  override def insertAirQuality(db: Database, air: Air)(implicit ec: ExecutionContext): Future[Int] = {
    val insertAction = db.run(airs += air)
    insertAction.onComplete {
      case Success(_) =>
        println("Air quality data inserted successfully")
      case Failure(e) =>
        println(s"Failed to insert air quality data: $e")
    }
    insertAction
  }

}