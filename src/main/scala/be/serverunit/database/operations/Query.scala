package be.serverunit.database.operations

import be.serverunit.database.Air
import be.serverunit.database.SlickTables.{airs, sessions, sets}
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.H2Profile.api.*
import slick.lifted.Functions.*
import slick.lifted.SimpleFunction

import java.time.LocalDateTime
import java.time.ZoneOffset
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global

object Query {

  def getLatestAirQuality(db: Database): Future[Option[Air]] = {
    val query = airs.sortBy(_.id.desc).result.headOption
    db.run(query)
  }

  private def extractYear(date: Rep[java.time.Instant]) = SimpleFunction.unary[java.time.Instant, Int]("YEAR").apply(date)
  private def extractMonth(date: Rep[java.time.Instant]) = SimpleFunction.unary[java.time.Instant, Int]("MONTH").apply(date)
  private def extractDay(date: Rep[java.time.Instant]) = SimpleFunction.unary[java.time.Instant, Int]("DAY").apply(date)

  private def filterSessions(userID: String, year: Int, month: Option[Int] = None, week: Option[Int] = None, day: Option[Int] = None) = {
    sessions.filter { session =>
      val yearMatch = extractYear(session.beginDate) === year
      val monthMatch = month.map(m => extractMonth(session.beginDate) === m: Rep[Boolean]).getOrElse(true: Rep[Boolean])
      val weekMatch = week.map(w => (extractDay(session.beginDate) - 1) / 7 + 1 === w: Rep[Boolean]).getOrElse(true: Rep[Boolean])
      val dayMatch = day.map(d => extractDay(session.beginDate) === d: Rep[Boolean]).getOrElse(true: Rep[Boolean])

      session.userID === userID && yearMatch && monthMatch && weekMatch && dayMatch
    }.map(_.id)
  }

  def getNumberOfSessions(db: Database, userID: String, year: Int, month: Option[Int] = None, week: Option[Int] = None, day: Option[Int] = None): Future[Option[Int]] = {
    val query = filterSessions(userID, year, month, week, day).length.result
    db.run(query).map(Some(_))
  }

  def getMeanExerciseTime(db: Database, userID: String, year: Int, month: Option[Int] = None, week: Option[Int] = None, day: Option[Int] = None): Future[Double] = {
    val sessionIdsQuery = filterSessions(userID, year, month, week, day).result

    val setsQuery = sessionIdsQuery.flatMap { sessionIds =>
      sets.filter(set => (set.sessionID inSet sessionIds) && set.endDate.isDefined).map { set =>
        (set.beginDate, set.endDate)
      }.result
    }

    db.run(setsQuery).map { sets =>
      val times = sets.collect { case (beginDate, Some(endDate)) =>
        java.time.Duration.between(beginDate, endDate).getSeconds
      }

      if (times.nonEmpty) times.sum.toDouble / times.length else 0.0
    }
  }
}