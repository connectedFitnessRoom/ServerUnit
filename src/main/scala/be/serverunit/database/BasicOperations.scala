package be.serverunit.database

import be.serverunit.database.SlickTables.*
import slick.jdbc.H2Profile.api.*

import scala.concurrent.Future

object BasicOperations {
  def insertSession(session: Session): Future[Int] = {
    db.run(sessions += session)
  }
  
  def getSession(id: Long): Future[Option[Session]] = {
    db.run(sessions.filter(_.id === id).result.headOption)
  }
  
  def getSessionByUserAndDate(userID: Long, date: java.time.LocalDate): Option[Session] = {
    db.run(sessions.filter(s => s.userID === userID && s.beginDate === date).result.headOption).await
  }
  
  def getSessionByUser(userID: Long): Future[Seq[Session]] = {
    db.run(sessions.filter(_.userID === userID).result)
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
  
  def getRepetitionBySetAndSession(setID: Int, sessionID: Long): Future[Seq[Repetition]] = {
    db.run(repetitions.filter(r => r.setID === setID && r.sessionID === sessionID).result)
  }
}
