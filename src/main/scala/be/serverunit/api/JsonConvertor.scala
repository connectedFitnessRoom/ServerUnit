package be.serverunit.api

import be.serverunit.database.Air
import play.api.libs.json.{JsObject, Json}

object JsonConvertor {

  def airToJson(airQuality: Air): String = {
    s"""{
       |  "env_data": {
       |    "temperature": ${airQuality.temperature},
       |    "humidity": ${airQuality.humidity},
       |    "particule": ${airQuality.ppm}
       |  }
       |}""".stripMargin
  }

  def numberOfSessionsToJson(period: String, year: Int, month: Option[Int], week: Option[Int], day: Option[Int], count: Option[Int], counts: Seq[Int]): String = {
    val periodJson = period match {
      case "year" => s""""year": $year, "year_count": ${count.getOrElse(0)}, "monthly_count": [${counts.mkString(", ")}]"""
      case "month" => s""""year": $year, "month": ${month.get}, "month_count": ${count.getOrElse(0)}, "weekly_count": [${counts.mkString(", ")}]"""
      case "week" => s""""year": $year, "month": ${month.get}, "week": ${week.get}, "week_count": ${count.getOrElse(0)}, "daily_count": [${counts.mkString(", ")}]"""
      case "day" => s""""year": $year, "month": ${month.get}, "week": ${week.get}, "day": ${day.get}, "day_count": ${count.getOrElse(0)}"""
    }
    s"""{
       |  "num_visits_$period": {
       |    $periodJson
       |  }
       |}""".stripMargin
  }

  def meanExerciseTimeToJson(period: String, year: Int, month: Option[Int], week: Option[Int], day: Option[Int], mean: Double, means: Seq[Double]): String = {
    val periodJson = period match {
      case "year" =>
        s""""year": $year, "year_moy": $mean, "monthly_moy": [${means.mkString(", ")}]"""
      case "month" =>
        s""""year": $year, "month": ${month.get}, "month_moy": $mean, "weekly_moy": [${means.mkString(", ")}]"""
      case "week" =>
        s""""year": $year, "month": ${month.get}, "week": ${week.get}, "week_moy": $mean, "daily_moy": [${means.mkString(", ")}]"""
      case "day" =>
        s""""year": $year, "month": ${month.get}, "week": ${week.get}, "day": ${day.get}, "day_moy": $mean"""
    }
    s"""{
       |  "exo_time_$period": {
       |    $periodJson
       |  }
       |}""".stripMargin
  }

  case class SetData(machine: Int, weight: Float, repetitions: Option[Int], setTime: String, distances: Seq[Int], times: Seq[Float])

  case class DetailedSessionData(sessionDuration: String, envData: (Double, Double, Double), sets: Seq[SetData])

  case class SessionData(sessionDuration: String, envData: (Double, Double, Double))
  
  def sessionDataToJson(year: Int, month: Int, day: Int, dayAvg: String, sessions: Seq[SessionData]): String = {
    val sessionJson = sessions.map { session =>
      s"""{
         |  "session_duration": "${session.sessionDuration}",
         |  "env_data": {
         |    "temperature": "${session.envData._1}",
         |    "humidity": "${session.envData._2}",
         |    "particulate": "${session.envData._3}"
         |  }
         |}""".stripMargin
    }.mkString("[", ", ", "]")

    s"""{
       |  "exo_time_day": {
       |    "year": $year,
       |    "month": $month,
       |    "day": $day,
       |    "day_avg": "$dayAvg",
       |    "sessions": $sessionJson
       |  }
       |}""".stripMargin
  }

  def detailedSessionDataToJson(year: Int, month: Int, day: Int, dayAvg: String, sessions: Seq[DetailedSessionData]): String = {
    val sessionJson = sessions.map { session =>
      val setsJson = session.sets.map { set =>
        s"""{
           |  "machine": "${set.machine}",
           |  "weight": "${set.weight}",
           |  "rep": "${set.repetitions.getOrElse(0)}",
           |  "set_time": "${set.setTime}",
           |  "distance": [${set.distances.mkString(", ")}],
           |  "times": [${set.times.mkString(", ")}]
           |}""".stripMargin
      }.mkString("[", ", ", "]")

      s"""{
         |  "session_duration": "${session.sessionDuration}",
         |  "env_data": {
         |    "temperature": "${session.envData._1}",
         |    "humidity": "${session.envData._2}",
         |    "particulate": "${session.envData._3}"
         |  },
         |  "sets": $setsJson
         |}""".stripMargin
    }.mkString("[", ", ", "]")

    s"""{
       |  "exo_time_day": {
       |    "year": $year,
       |    "month": $month,
       |    "day": $day,
       |    "day_avg": "$dayAvg",
       |    "sessions": $sessionJson
       |  }
       |}""".stripMargin
  }

  def sessionCountToJson(year: Int, month: Int, day: Int, sessionCount: Int, sessions: Seq[DetailedSessionData]): JsObject = {
    val sessionJson = sessions.map { session =>
      val setsJson = session.sets.map { set =>
        Json.obj(
          "machine" -> set.machine,
          "weight" -> set.weight,
          "rep" -> set.repetitions.getOrElse(0),
          "set_time" -> set.setTime,
          "distance" -> set.distances,
          "times" -> set.times
        )
      }

      Json.obj(
        "session_duration" -> session.sessionDuration,
        "env_data" -> Json.obj(
          "temperature" -> session.envData._1,
          "humidity" -> session.envData._2,
          "particulate" -> session.envData._3
        ),
        "sets" -> setsJson
      )
    }

    Json.obj(
      "year" -> year,
      "month" -> month,
      "day" -> day,
      "session_count" -> sessionCount,
      "sessions" -> sessionJson
    )
  }
}