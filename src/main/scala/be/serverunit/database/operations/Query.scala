package be.serverunit.database.operations

import be.serverunit.database.Air
import be.serverunit.database.SlickTables.{airs, sessions, sets}
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.H2Profile.api.*
import slick.lifted.Functions.*

import java.time.LocalDateTime
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global

object Query {

  def getLatestAirQuality(db: Database): Future[Option[Air]] = {
    val query = airs.sortBy(_.id.desc).result.headOption
    db.run(query)
  }

  def getNumberOfSessionsByYearByUser(db: Database, userID: String, year: Int): Future[Option[Int]] = {
    val query = sessions.filter { session =>
      session.userID === userID && session.beginDate.asColumnOf[LocalDateTime].getYear === year
    }.length.result

    db.run(query).map(Some(_))
  }

  def getNumberOfSessionsByMonthByUser(db: Database, userID: String, year: Int, month: Int): Future[Option[Int]] = {
    val query = sessions.filter { session =>
      session.userID === userID &&
        session.beginDate.asColumnOf[LocalDateTime].getYear === year &&
        session.beginDate.asColumnOf[LocalDateTime].getMonth === month
    }.length.result

    db.run(query).map(Some(_))
  }

  def getNumberOfSessionsByWeekByUser(db: Database, userID: String, year: Int, month: Int, startDay: Int): Future[Option[Int]] = {
    val query = sessions.filter { session =>
      session.userID === userID &&
        session.beginDate.asColumnOf[LocalDateTime].getYear === year &&
        session.beginDate.asColumnOf[LocalDateTime].getMonth === month &&
        session.beginDate.asColumnOf[LocalDateTime].getDay >= startDay &&
        session.beginDate.asColumnOf[LocalDateTime].getDay < startDay + 7
    }.length.result

    db.run(query).map(Some(_))
  }

  def getNumberOfSessionsByDayByUser(db: Database, userID: String, year: Int, month: Int, day: Int): Future[Option[Int]] = {
    val query = sessions.filter { session =>
      session.userID === userID &&
        session.beginDate.asColumnOf[LocalDateTime].getYear === year &&
        session.beginDate.asColumnOf[LocalDateTime].getMonth === month &&
        session.beginDate.asColumnOf[LocalDateTime].getDay === day
    }.length.result

    db.run(query).map(Some(_))
  }

  implicit class LocalDateTimeColumnOps(val col: Rep[LocalDateTime]) extends AnyVal {
    def getYear: Rep[Int] = col.asColumnOf[String].substring(0, 4).asColumnOf[Int]
    def getMonth: Rep[Int] = col.asColumnOf[String].substring(5, 7).asColumnOf[Int]
    def getDay: Rep[Int] = col.asColumnOf[String].substring(8, 10).asColumnOf[Int]
  }


  def getMeanExerciseTimeByMonthByUser(db: Database, userID: String, year: Int, month: Int): Future[Option[Double]] = {
    val sessionIdsQuery = sessions.filter { session =>
      session.userID === userID &&
        session.beginDate.asColumnOf[LocalDateTime].getYear === year &&
        session.beginDate.asColumnOf[LocalDateTime].getMonth === month
    }.map(_.id).result

    val setsQuery = sessionIdsQuery.flatMap { sessionIds =>
      sets.filter(_.sessionID inSet sessionIds).map { set =>
        set.endDate.asColumnOf[Long] - set.beginDate.asColumnOf[Long]
      }.result
    }

    db.run(setsQuery).map { times =>
      if (times.nonEmpty) Some(times.sum.toDouble / times.length) else None
    }
  }

  def getMeanExerciseTimeByWeekByUser(db: Database, userID: String, year: Int, month: Int, startDay: Int): Future[Option[Double]] = {
    val sessionIdsQuery = sessions.filter { session =>
      session.userID === userID &&
        session.beginDate.asColumnOf[LocalDateTime].getYear === year &&
        session.beginDate.asColumnOf[LocalDateTime].getMonth === month &&
        session.beginDate.asColumnOf[LocalDateTime].getDay >= startDay &&
        session.beginDate.asColumnOf[LocalDateTime].getDay < startDay + 7
    }.map(_.id).result

    val setsQuery = sessionIdsQuery.flatMap { sessionIds =>
      sets.filter(_.sessionID inSet sessionIds).map { set =>
        set.endDate.asColumnOf[Long] - set.beginDate.asColumnOf[Long]
      }.result
    }

    db.run(setsQuery).map { times =>
      if (times.nonEmpty) Some(times.sum.toDouble / times.length) else None
    }
  }

  def getMeanExerciseTimeByDayByUser(db: Database, userID: String, year: Int, month: Int, day: Int): Future[Option[Double]] = {
    val sessionIdsQuery = sessions.filter { session =>
      session.userID === userID &&
        session.beginDate.asColumnOf[LocalDateTime].getYear === year &&
        session.beginDate.asColumnOf[LocalDateTime].getMonth === month &&
        session.beginDate.asColumnOf[LocalDateTime].getDay === day
    }.map(_.id).result

    val setsQuery = sessionIdsQuery.flatMap { sessionIds =>
      sets.filter(_.sessionID inSet sessionIds).map { set =>
        set.endDate.asColumnOf[Long] - set.beginDate.asColumnOf[Long]
      }.result
    }

    db.run(setsQuery).map { times =>
      if (times.nonEmpty) Some(times.sum.toDouble / times.length) else None
    }
  }
}