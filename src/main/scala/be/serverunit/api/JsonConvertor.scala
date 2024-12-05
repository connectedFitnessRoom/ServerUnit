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

  def convertNumberOfSessionsToJson(period: String, year: Int, month: Option[Int], week: Option[Int], count: Option[Int] = None, counts: Seq[Int] = Seq()): String = {
    val periodJson = period match {
      case "year" => s""""year": $year, "year_count": ${count.getOrElse(0)}, "monthly_count": [${counts.mkString(", ")}]"""
      case "month" => s""""year": $year, "month": ${month.get}, "month_count": ${count.getOrElse(0)}, "weekly_count": [${counts.mkString(", ")}]"""
      case "week" => s""""year": $year, "month": ${month.get}, "week": ${week.get}, "week_count": ${count.getOrElse(0)}, "daily_count": [${counts.mkString(", ")}]"""
    }
    s"""{
       |  "num_visits_$period": {
       |    $periodJson
       |  }
       |}""".stripMargin
  }

  def convertMeanExerciseTimeToJson(period: String, year: Int, month: Option[Int], week: Option[Int], mean: Double, means: Seq[Double]): String = {
    val periodJson = period match {
      case "year" =>
        s""""year": $year, "year_moy": $mean, "monthly_moy": [${means.mkString(", ")}]"""
      case "month" =>
        s""""year": $year, "month": ${month.get}, "month_moy": $mean, "weekly_moy": [${means.mkString(", ")}]"""
      case "week" =>
        s""""year": $year, "month": ${month.get}, "week": ${week.get}, "week_moy": $mean, "daily_moy": [${means.mkString(", ")}]"""
    }
    s"""{
       |  "exo_time_$period": {
       |    $periodJson
       |  }
       |}""".stripMargin
  }

}