package be.serverunit.api

import be.serverunit.api.JsonConvertor.*
import be.serverunit.database.operations.Query.*
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.{ExecutionContext, Future}

object HttpFetch {

  def fetchAirQuality(db: Database)(implicit ec: ExecutionContext): Future[String] = {
    getLatestAirQuality(db).map {
      case Some(airQuality) =>
        convertAirQualityToJson(airQuality)
      case None =>
        "No data found"
    }.recover {
      case e: Exception =>
        s"Error: ${e.getMessage}"
    }
  }

  def fetchNumberOfSessionsByYear(db: Database, userID: String, year: Int)(implicit ec: ExecutionContext): Future[String] = {
    val monthFutures = (1 to 12).map { month =>
      getNumberOfSessionsByMonthByUser(db, userID, year, month).map {
        case Some(count) => count
        case None => 0
      }
    }

    for {
      monthlyCounts <- Future.sequence(monthFutures)
    } yield {
      val yearCount = monthlyCounts.sum
      visitByYear(year, yearCount, monthlyCounts)
    }
  }

  def fetchNumberOfSessionsByMonth(db: Database, userID: String, year: Int, month: Int)(implicit ec: ExecutionContext): Future[String] = {
    val weekFutures = (1 to 5).map { week =>
      getNumberOfSessionsByWeekByUser(db, userID, year, month, (week - 1) * 7 + 1).map {
        case Some(count) => count
        case None => 0
      }
    }

    for {
      weeklyCounts <- Future.sequence(weekFutures)
    } yield {
      val monthCount = weeklyCounts.sum
      visitByMonth(year, month, monthCount, weeklyCounts)
    }
  }

  def fetchNumberOfSessionsByWeek(db: Database, userID: String, year: Int, month: Int, week: Int)(implicit ec: ExecutionContext): Future[String] = {
    val dayFutures = (1 to 7).map { day =>
      getNumberOfSessionsByDayByUser(db, userID, year, month, (week - 1) * 7 + day).map {
        case Some(count) => count
        case None => 0
      }
    }

    for {
      dailyCounts <- Future.sequence(dayFutures)
    } yield {
      val weekCount = dailyCounts.sum
      visitByWeek(year, month, week, weekCount, dailyCounts)
    }
  }


  def fetchMeanExerciseTimeByYear(db: Database, userID: String, year: Int)(implicit ec: ExecutionContext): Future[String] = {
    val monthFutures = (1 to 12).map { month =>
      getMeanExerciseTimeByMonthByUser(db, userID, year, month).map {
        case Some(meanTime) => meanTime
        case None => 0.0
      }
    }

    for {
      monthlyMeans <- Future.sequence(monthFutures)
    } yield {
      val yearMean = if (monthlyMeans.nonEmpty) monthlyMeans.sum / monthlyMeans.length else 0.0
      exerciseTimeByYear(year, yearMean, monthlyMeans)
    }
  }

  def fetchMeanExerciseTimeByMonth(db: Database, userID: String, year: Int, month: Int)(implicit ec: ExecutionContext): Future[String] = {
    val weekFutures = (1 to 5).map { week =>
      getMeanExerciseTimeByWeekByUser(db, userID, year, month, (week - 1) * 7 + 1).map {
        case Some(meanTime) => meanTime
        case None => 0.0
      }
    }

    for {
      weeklyMeans <- Future.sequence(weekFutures)
    } yield {
      val monthMean = if (weeklyMeans.nonEmpty) weeklyMeans.sum / weeklyMeans.length else 0.0
      exerciseTimeByMonth(year, month, monthMean, weeklyMeans)
    }
  }

  def fetchMeanExerciseTimeByWeek(db: Database, userID: String, year: Int, month: Int, week: Int)(implicit ec: ExecutionContext): Future[String] = {
    val dayFutures = (1 to 7).map { day =>
      getMeanExerciseTimeByDayByUser(db, userID, year, month, (week - 1) * 7 + day).map {
        case Some(meanTime) => meanTime
        case None => 0.0
      }
    }

    for {
      dailyMeans <- Future.sequence(dayFutures)
    } yield {
      val weekMean = if (dailyMeans.nonEmpty) dailyMeans.sum / dailyMeans.length else 0.0
      exerciseTimeByWeek(year, month, week, weekMean, dailyMeans)
    }
  }
}