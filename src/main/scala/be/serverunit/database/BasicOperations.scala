package be.serverunit.database

import be.serverunit.database.SlickTables.*
import slick.jdbc.H2Profile.api.*
import be.serverunit.database.Session
import slick.jdbc.JdbcBackend.Database
import scala.concurrent.Future

object BasicOperations {
  def insertSession(db: Database, session: Session): Future[Int] = {
    db.run(sessions += session)
  }
  
  def getSession(db: Database, id: Long): Future[Option[Session]] = {
    db.run(sessions.filter(_.id === id).result.headOption)
  }
  
  def getSessionByUser(db: Database, userID: String): Future[Seq[Session]] = {
    db.run(sessions.filter(_.userID === userID).result)
  }

  def getLastSessionByUser(db: Database, userID: String): Future[Option[Session]] = {
    db.run(sessions.filter(_.userID === userID).sortBy(_.id.desc).result.headOption)
  }
  
  def insertSet(db: Database, set: Set): Future[Int] = {
    db.run(sets += set)
  }
  
  def getSetBySession(db: Database, sessionID: Long): Future[Seq[Set]] = {
    db.run(sets.filter(_.sessionID === sessionID).result)
  }
  
  def getSetByMachine(db: Database, machineID: Int): Future[Seq[Set]] = {
    db.run(sets.filter(_.machineID === machineID).result)
  }
  
  def getLastSetBySessionAndMachine(db: Database, sessionID: Long, machineID: Int): Future[Option[Set]] = {
    db.run(sets.filter(s => s.sessionID === sessionID && s.machineID === machineID).result.headOption)
  }

  def insertRepetition(db: Database, repetition: Repetition): Future[Int] = {
    db.run(repetitions += repetition)
  }
  
}