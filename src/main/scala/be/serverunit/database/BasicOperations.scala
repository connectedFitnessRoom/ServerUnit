package be.serverunit.database

import be.serverunit.database.DatabaseApp.db
import be.serverunit.database.SlickTables.*
import slick.jdbc.H2Profile.api.*

import be.serverunit.database.Session

import scala.concurrent.{Await, Future}

object BasicOperations {
  def insertSession(session: Session): Future[Int] = {
    db.run(sessions += session)
  }
  
  def getSession(id: Long): Future[Option[Session]] = {
    db.run(sessions.filter(_.id === id).result.headOption)
  }
  
  def getSessionByUser(userID: String): Future[Seq[Session]] = {
    db.run(sessions.filter(_.userID === userID).result)
  }

  def getLastSessionByUser(userID: String): Future[Option[Session]] = {
    db.run(sessions.filter(_.userID === userID).sortBy(_.id.desc).result.headOption)
  }
  
  def insertSet(set: Set): Future[Int] = {
    db.run(sets += set)
  }
  
  def getSetBySession(sessionID: Long): Future[Seq[Set]] = {
    db.run(sets.filter(_.sessionID === sessionID).result)
  }
  
  def getSetByMachine(machineID: Int): Future[Seq[Set]] = {
    db.run(sets.filter(_.machineID === machineID).result)
  }
  
  def getLastSetBySessionAndMachine(sessionID: Long, machineID: Int): Future[Option[Set]] = {
    db.run(sets.filter(s => s.sessionID === sessionID && s.machineID === machineID).result.headOption)
  }

  def insertRepetition(repetition: Repetition): Future[Int] = {
    db.run(repetitions += repetition)
  }
  
  def getRepetitionBySetAndSession(setID: Int, sessionID: Long): Future[Seq[Repetition]] = {
    db.run(repetitions.filter(r => r.setID === setID && r.sessionID === sessionID).result)
  }
}
