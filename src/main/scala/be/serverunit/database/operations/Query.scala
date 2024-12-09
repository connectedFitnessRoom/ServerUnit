package be.serverunit.database.operations

import be.serverunit.database.SlickTables.{airs, repetitions, sessions, sets}
import be.serverunit.database.{Air, UserSession}
import slick.jdbc.H2Profile.api.*
import slick.jdbc.JdbcBackend.Database
import slick.lifted.SimpleFunction

import java.time.{Duration, Instant}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

object Query {

  def getLatestAirQuality(db: Database): Future[Option[Air]] = {
    val query = airs.sortBy(_.id.desc).result.headOption
    db.run(query)
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
      val times = sets.collect { case (beginDate, Some(endDate)) => java.time.Duration.between(beginDate, endDate).getSeconds
      }

      if (times.nonEmpty) times.sum.toDouble / times.length else 0.0
    }
  }

  private def filterSessions(userID: String, year: Int, month: Option[Int] = None, week: Option[Int] = None, day: Option[Int] = None) = {
    sessions.filter { session =>
      val yearMatch = extractYear(session.beginDate) === year
      val monthMatch = month.map(m => extractMonth(session.beginDate) === m: Rep[Boolean]).getOrElse(true: Rep[Boolean])
      val weekMatch = week.map { w => (((extractDay(session.beginDate) - 1) / 7) + 1) === w }.getOrElse(true: Rep[Boolean])
      val dayMatch = day.map(d => extractDay(session.beginDate) === d: Rep[Boolean]).getOrElse(true: Rep[Boolean])

      session.userID === userID && yearMatch && monthMatch && weekMatch && dayMatch
    }.map(_.id)
  }

  private def extractYear(date: Rep[java.time.Instant]) = SimpleFunction.unary[java.time.Instant, Int]("YEAR").apply(date)

  private def extractMonth(date: Rep[java.time.Instant]) = SimpleFunction.unary[java.time.Instant, Int]("MONTH").apply(date)

  private def extractDay(date: Rep[java.time.Instant]) = SimpleFunction.unary[java.time.Instant, Int]("DAY").apply(date)

  def getSessionByUserIDByDate(db: Database, userID: String, beginDate: Instant, endDate: Instant)(implicit ec: ExecutionContext): Future[Seq[UserSession]] = {
    val query = sessions.filter(session => session.userID === userID && session.beginDate >= beginDate && session.beginDate < endDate).result
    db.run(query)
  }

  def getSetDataBySessionID(db: Database, sessionID: Long)(implicit ec: ExecutionContext): Future[Seq[(Int, Float, Option[Int], String, List[Int], List[Float])]] = {
    val setQuery = sets.filter(_.sessionID === sessionID).result

    db.run(setQuery).flatMap { setsList =>
      Future.sequence(setsList.map { set =>
        getDistancesAndTimesBySetID(db, set.id).map { case (distances, times) =>
          val setTime = s"${set.endDate.getOrElse("Unknown")} - ${set.beginDate}"
          (set.machineID, set.weight, set.repetitions, setTime, distances, times)
        }
      }).map(_.toSeq)
    }
  }

  def getDistancesAndTimesBySetID(db: Database, setID: Long)(implicit ec: ExecutionContext): Future[(List[Int], List[Float])] = {
    val query = for {
      set <- sets if set.id === setID
      repetition <- repetitions if repetition.setID === set.id
    } yield (repetition.distance, repetition.timer)

    db.run(query.result).map { result =>
      val distances = result.map(_._1).toList
      val times = result.map(_._2).toList
      (distances, times)
    }
  }

  def getSessionDuration(db: Database, sessionID: Long)(implicit ec: ExecutionContext): Future[Option[Long]] = {
    val query = sessions.filter(_.id === sessionID).result.headOption

    db.run(query).map {
      case Some(session) =>
        session.endDate match {
          case Some(endDate) => Some(Duration.between(session.beginDate, endDate).getSeconds)
          case None => None // Session is still ongoing or endDate is not set
        }
      case None => None // Session not found
    }
  }

  def getAverageEnvDataBySession(db: Database, sessionID: Long)(implicit ec: ExecutionContext): Future[Option[(Double, Double, Double)]] = {
    val sessionQuery = sessions.filter(_.id === sessionID).result.headOption

    val airQuery = sessionQuery.flatMap {
      case Some(session) =>
        airs.filter(air => air.timestamp >= session.beginDate && air.timestamp <= session.endDate.getOrElse(Instant.now())).result
      case None => DBIO.successful(Seq.empty)
    }

    db.run(airQuery).map { airData =>
      if (airData.nonEmpty) {
        val avgTemperature = airData.map(_.temperature).sum / airData.length
        val avgHumidity = airData.map(_.humidity).sum / airData.length
        val avgPpm = airData.map(_.ppm).sum / airData.length
        Some((avgTemperature, avgHumidity, avgPpm))
      } else {
        None
      }
    }
  }

}