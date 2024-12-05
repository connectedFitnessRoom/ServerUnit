package be.serverunit.api

import be.serverunit.api.JsonConvertor.*
import be.serverunit.database.operations.Query.*
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.{ExecutionContext, Future}

object HttpFetch {

  def fetchAirQuality(db: Database)(implicit ec: ExecutionContext): Future[String] = {
    getLatestAirQuality(db).map {
      case Some(airQuality) => airQuality.toJson
      case None => "No data found"
    }.recover {
      case e: Exception => s"Error: ${e.getMessage}"
    }
  }

  private def fetchMeanExerciseTime(db: Database, userID: String, year: Int, period: String, month: Option[Int] = None, week: Option[Int] = None)(implicit ec: ExecutionContext): Future[String] = {
    val futures = period match {
      case "year" => (1 to 12).map(m => getMeanExerciseTime(db, userID, year, Some(m)))
      case "month" => (1 to 5).map(w => getMeanExerciseTime(db, userID, year, month, Some(w)))
      case "week" => (1 to 7).map(d => getMeanExerciseTime(db, userID, year, month, week, Some((week.get - 1) * 7 + d)))
    }

    for {
      means <- Future.sequence(futures)
    } yield {
      val mean = if (means.nonEmpty) means.sum / means.length else 0.0
      period.toJson(year, month, week, mean, means)
    }
  }

  private def fetchNumberOfSessions(db: Database, userID: String, year: Int, period: String, month: Option[Int] = None, week: Option[Int] = None)(implicit ec: ExecutionContext): Future[String] = {
    val futures = period match {
      case "year" => (1 to 12).map(m => getNumberOfSessions(db, userID, year, Some(m)))
      case "month" => (1 to 5).map(w => getNumberOfSessions(db, userID, year, month, Some(w)))
      case "week" => (1 to 7).map(d => getNumberOfSessions(db, userID, year, month, week, Some((week.get - 1) * 7 + d)))
    }

    for {
      counts <- Future.sequence(futures)
    } yield {
      val count = counts.flatten.sum
      period.toJson(year, month, week, Some(count), counts.flatten)
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

  def fetchNumberOfSessionsByYear(db: Database, userID: String, year: Int)(implicit ec: ExecutionContext): Future[String] = {
    fetchNumberOfSessions(db, userID, year, "year")
  }

  def fetchNumberOfSessionsByMonth(db: Database, userID: String, year: Int, month: Int)(implicit ec: ExecutionContext): Future[String] = {
    fetchNumberOfSessions(db, userID, year, "month", Some(month))
  }

  def fetchNumberOfSessionsByWeek(db: Database, userID: String, year: Int, month: Int, week: Int)(implicit ec: ExecutionContext): Future[String] = {
    fetchNumberOfSessions(db, userID, year, "week", Some(month), Some(week))
  }
}