package be.serverunit.database.operations

import be.serverunit.database.SlickTables.*
import be.serverunit.database.{Repetition, Session, Set}
import be.serverunit.traits.database.{RepetitionOperations, SessionOperations, SetOperations}
import slick.jdbc.H2Profile.api.*
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.{ExecutionContext, Future}

object Basic extends SessionOperations with SetOperations with RepetitionOperations {

  // Session operations
  override def getLastSessionByUser(db: Database, userID: String): Future[Option[Session]] = {
    db.run(sessions.filter(_.userID === userID).sortBy(_.id.desc).result.headOption)
  }
  

  // Set operations
  override def insertSetWithReturn(db: Database, set: Set)(implicit ec: ExecutionContext): Future[Set] = {
    db.run((sets returning sets.map(_.id)) += set).map(generatedId => set.copy(id = generatedId))
  }

  override def updateSet(db: Database, set: Set): Future[Int] = {
    db.run(sets.filter(_.id === set.id).update(set))
  }

  // Repetition operations
  override def insertRepetition(db: Database, repetition: Repetition): Future[Int] = {
    db.run(repetitions += repetition)
  }
}
