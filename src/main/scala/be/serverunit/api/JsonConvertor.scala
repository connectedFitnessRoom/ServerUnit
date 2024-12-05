package be.serverunit.api

import be.serverunit.database.Air

object JsonConvertor {

  def convertAirQualityToJson(airQuality: Air): String = {
    s"""{
       |  "env_data": {
       |    "temperature": ${airQuality.temperature},
       |    "humidity": ${airQuality.humidity},
       |    "particule": ${airQuality.ppm}
       |  }
       |}""".stripMargin
  }


  def visitByYear(year: Int, yearCount: Int, monthlyCount: Seq[Int]): String = {
    s"""{
       |  "num_visits_year": {
       |    "year": $year,
       |    "year_count": $yearCount,
       |    "monthly_count": [${monthlyCount.mkString(", ")}]
       |  }
       |}""".stripMargin
  }

    def visitByMonth(year: Int, month: Int, monthCount: Int, weeklyCounts: Seq[Int]): String = {
      s"""{
         |  "num_visits_month": {
         |    "year": $year,
         |    "month": $month,
         |    "month_count": $monthCount,
         |    "weekly_count": [${weeklyCounts.mkString(", ")}]
         |  }
         |}""".stripMargin

  }

  def visitByWeek(year: Int, month: Int, week: Int, weekCount: Int, dailyCounts: Seq[Int]): String = {
    s"""{
       |  "num_visits_week": {
       |    "year": $year,
       |    "month": $month,
       |    "week": $week,
       |    "week_count": $weekCount,
       |    "daily_count": [${dailyCounts.mkString(", ")}]
       |  }
       |}""".stripMargin
  }

  def exerciseTimeByYear(year: Int, yearMean: Double, monthlyMeans: Seq[Double]): String = {
    s"""
      {
        "exo_time_year": {
          "year": $year,
          "year_moy": $yearMean,
          "monthly_moy": [${monthlyMeans.mkString(",")}]
        }
      }
      """
  }

  def exerciseTimeByMonth(year: Int, month: Int, monthMean: Double, weeklyMeans: Seq[Double]): String = {
    s"""
      {
        "exo_time_month": {
          "year": $year,
          "month": $month,
          "month_moy": $monthMean,
          "weekly_moy": [${weeklyMeans.mkString(",")}]
        }
      }
      """
  }
  
  def exerciseTimeByWeek(year: Int, month: Int, week: Int, weekMean: Double, dailyMeans: Seq[Double]): String = {
    s"""
      {
        "exo_time_week": {
          "year": $year,
          "month": $month,
          "week": $week,
          "week_moy": $weekMean,
          "daily_moy": [${dailyMeans.mkString(",")}]
        }
      }
      """
  }
}
