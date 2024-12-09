package be.serverunit.api

import be.serverunit.api.JsonConvertor.*
import be.serverunit.database.operations.Query.*
import play.api.libs.json.Json
import slick.jdbc.JdbcBackend.Database

import java.time.{LocalDateTime, ZoneOffset}
import scala.concurrent.{ExecutionContext, Future}

object HttpFetch {

  def fetchAirQuality(db: Database)(implicit ec: ExecutionContext): Future[String] = {
    getLatestAirQuality(db).map {
      case Some(airQuality) => Json.prettyPrint(airToJson(airQuality))
      case None => "No air quality data available"
    }.recover {
      case e: Exception => s"Error: ${e.getMessage}"
    }
  }

  def fetchMeanExerciseTimeByYear(db: Database, userID: String, year: Int)(implicit ec: ExecutionContext): Future[String] = {
    fetchMeanExerciseTime(db, userID, year, "year")
  }

  def fetchMeanExerciseTimeByMonth(db: Database, userID: String, year: Int, month: Int)(implicit ec: ExecutionContext): Future[String] = {
    fetchMeanExerciseTime(db, userID, year, "month", Some(month))
  }

  def fetchMeanExerciseTimeByWeek(db: Database, userID: String, year: Int, month: Int, week: Int)(implicit ec: ExecutionContext): Future[String] = {
    fetchMeanExerciseTime(db, userID, year, "week", Some(month), Some(week))
  }

  private def fetchMeanExerciseTime(db: Database, userID: String, year: Int, period: String, month: Option[Int] = None, week: Option[Int] = None, day: Option[Int] = None)(implicit ec: ExecutionContext): Future[String] = {
    val futures = period match {
      case "year" => (1 to 12).map(m => getMeanExerciseTime(db, userID, year, Some(m)))
      case "month" => (1 to 5).map(w => getMeanExerciseTime(db, userID, year, month, Some(w)))
      case "week" => (1 to 7).map(d => getMeanExerciseTime(db, userID, year, month, week, Some((week.get - 1) * 7 + d)))
      case "day" => Seq(getMeanExerciseTime(db, userID, year, month, week, day))
    }

    for {
      means <- Future.sequence(futures)
    } yield {
      val mean = if (means.nonEmpty) means.sum / means.length else 0.0
      Json.prettyPrint(meanExerciseTimeToJson(period, year, month, week, day, mean, means))
    }
  }

  def fetchMeanExerciseTimeByDay(db: Database, userID: String, year: Int, month: Int, week: Int, day: Int)(implicit ec: ExecutionContext): Future[String] = {
    fetchMeanExerciseTime(db, userID, year, "day", Some(month), Some(week), Some(day))
  }

  def fetchNumberOfSessionsByYear(db: Database, userID: String, year: Int)(implicit ec: ExecutionContext): Future[String] = {
    fetchNumberOfSessions(db, userID, year, "year")
  }

  def fetchNumberOfSessionsByMonth(db: Database, userID: String, year: Int, month: Int)(implicit ec: ExecutionContext): Future[String] = {
    fetchNumberOfSessions(db, userID, year, "month", Some(month))
  }

  private def fetchNumberOfSessions(db: Database, userID: String, year: Int, period: String, month: Option[Int] = None, week: Option[Int] = None, day: Option[Int] = None)(implicit ec: ExecutionContext): Future[String] = {
    val futures = period match {
      case "year" => (1 to 12).map(m => getNumberOfSessions(db, userID, year, Some(m)))
      case "month" => (1 to 5).map(w => getNumberOfSessions(db, userID, year, month, Some(w)))
      case "week" => (1 to 7).map(d => getNumberOfSessions(db, userID, year, month, week, Some((week.get - 1) * 7 + d)))
      case "day" => Seq(getNumberOfSessions(db, userID, year, month, week, day))
    }

    for {
      counts <- Future.sequence(futures)
    } yield {
      val flattenedCounts = counts.flatten
      val count = flattenedCounts.sum
      Json.prettyPrint(numberOfSessionsToJson(period, year, month, week, day, Some(count), flattenedCounts))
    }
  }

  def fetchNumberOfSessionsByWeek(db: Database, userID: String, year: Int, month: Int, week: Int)(implicit ec: ExecutionContext): Future[String] = {
    fetchNumberOfSessions(db, userID, year, "week", Some(month), Some(week))
  }

  def fetchNumberOfSessionsByDay(db: Database, userID: String, year: Int, month: Int, week: Int, day: Int)(implicit ec: ExecutionContext): Future[String] = {
    fetchNumberOfSessions(db, userID, year, "day", Some(month), Some(week), Some(day))
  }

  def fetchSessionData(db: Database, userID: String, year: Int, month: Int, day: Int)(implicit ec: ExecutionContext): Future[String] = {
    val startOfDay = LocalDateTime.of(year, month, day, 0, 0).toInstant(ZoneOffset.UTC)
    val endOfDay = startOfDay.plusSeconds(86400)

    for {
      sessions <- getSessionByUserIDByDate(db, userID, startOfDay, endOfDay)
      sessionData <- Future.sequence(sessions.map { session =>
        for {
          sessionDuration <- getSessionDuration(db, session.id)
          envData <- getAverageEnvDataBySession(db, session.id)
        } yield {
          JsonConvertor.SessionData(
            sessionDuration = s"${session.endDate.getOrElse("Ongoing")} - ${session.beginDate}",
            envData = envData.getOrElse((0.0, 0.0, 0.0))
          )
        }
      })
      totalDuration = sessionData.flatMap(_.sessionDuration.split(" - ").headOption.map(_.toLong)).sum
      avgDuration = if (sessionData.nonEmpty) totalDuration / sessionData.length else 0L
    } yield {
      Json.prettyPrint(sessionDataToJson(year, month, day, avgDuration.toString, sessionData))
    }
  }

  def fetchDetailedSessionData(db: Database, userID: String, year: Int, month: Int, day: Int)(implicit ec: ExecutionContext): Future[String] = {
    val startOfDay = LocalDateTime.of(year, month, day, 0, 0).toInstant(ZoneOffset.UTC)
    val endOfDay = startOfDay.plusSeconds(86400)

    for {
      sessions <- getSessionByUserIDByDate(db, userID, startOfDay, endOfDay)
      sessionData <- Future.sequence(sessions.map { session =>
        for {
          sessionDuration <- getSessionDuration(db, session.id)
          envData <- getAverageEnvDataBySession(db, session.id)
          setData <- getSetDataBySessionID(db, session.id)
        } yield {
          JsonConvertor.DetailedSessionData(
            sessionDurationString = s"${session.endDate.getOrElse("Ongoing")} - ${session.beginDate}",
            envData = envData.getOrElse((0.0, 0.0, 0.0)),
            sets = setData.map { case (machineID, weight, repetitions, setTime, distances, times) =>
              JsonConvertor.SetData(
                machine = machineID,
                weight = weight,
                repetitions = repetitions,
                setTime = setTime,
                distances = distances,
                times = times
              )
            }
          )
        }
      })
      totalDuration = sessionData.flatMap(_.sessionDurationString.split(" - ").headOption.map(_.toLong)).sum
      avgDuration = if (sessionData.nonEmpty) totalDuration / sessionData.length else 0L
    } yield {
      Json.prettyPrint(detailedSessionDataToJson(year, month, day, avgDuration.toString, sessionData))
    }
  }

  def fetchSessionCountForDay(db: Database, userID: String, year: Int, month: Int, day: Int)(implicit ec: ExecutionContext): Future[String] = {
    val startOfDay = LocalDateTime.of(year, month, day, 0, 0).toInstant(ZoneOffset.UTC)
    val endOfDay = startOfDay.plusSeconds(86400)

    for {
      sessions <- getSessionByUserIDByDate(db, userID, startOfDay, endOfDay)
      sessionData <- Future.sequence(sessions.map { session =>
        for {
          sessionDuration <- getSessionDuration(db, session.id)
          envData <- getAverageEnvDataBySession(db, session.id)
          setData <- getSetDataBySessionID(db, session.id)
        } yield {
          JsonConvertor.DetailedSessionData(
            sessionDurationString = s"${session.endDate.getOrElse("Ongoing")} - ${session.beginDate}",
            envData = envData.getOrElse((0.0, 0.0, 0.0)),
            sets = setData.map { case (machineID, weight, repetitions, setTime, distances, times) =>
              JsonConvertor.SetData(
                machine = machineID,
                weight = weight,
                repetitions = repetitions,
                setTime = setTime,
                distances = distances,
                times = times
              )
            }
          )
        }
      })
      sessionCount = sessionData.length
    } yield {
      Json.prettyPrint(sessionCountToJson(year, month, day, sessionCount, sessionData))
    }
  }

}