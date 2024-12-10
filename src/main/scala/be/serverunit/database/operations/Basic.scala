package be.serverunit.database.operations

import be.serverunit.database.SlickTables.*
import be.serverunit.database.{Air, Repetition, UserSet, UserSession}
import be.serverunit.traits.database.{AirOperations, RepetitionOperations, SessionOperations, SetOperations}
import slick.jdbc.H2Profile.api.*
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.{ExecutionContext, Future}

object Basic extends SessionOperations with SetOperations with RepetitionOperations with AirOperations {

  // Session operations
  override def getLastSessionByUser(db: Database, userID: String)(implicit ec: ExecutionContext): Future[Option[UserSession]] = {
    db.run(sessions.filter(_.userID === userID).sortBy(_.id.desc).result.headOption)
  }

  // Set operations
  override def insertSetWithReturn(db: Database, set: UserSet)(implicit ec: ExecutionContext): Future[UserSet] = {
    db.run((sets returning sets.map(_.id)) += set).map(generatedId => set.copy(id = generatedId))
  }

  override def updateASet(db: Database, set: UserSet)(implicit ec: ExecutionContext): Future[Int] = {
    db.run(sets.filter(_.id === set.id).update(set))
  }

  // Repetition operations
  override def insertRepetition(db: Database, repetition: Repetition)(implicit ec: ExecutionContext): Future[Int] = {
    db.run(repetitions += repetition)
  }

  // Air operations
  override def insertAirQuality(db: Database, air: Air)(implicit ec: ExecutionContext): Future[Int] = {
    db.run(airs += air)
  }

}