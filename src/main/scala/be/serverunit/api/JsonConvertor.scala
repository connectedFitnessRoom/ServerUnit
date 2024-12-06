package be.serverunit.api

import be.serverunit.database.Air
import play.api.libs.json._

object JsonConvertor {

  implicit val setDataWrites: Writes[SetData] = Json.writes[SetData]
  implicit val detailedSessionDataWrites: Writes[DetailedSessionData] = Json.writes[DetailedSessionData]
  implicit val sessionDataWrites: Writes[SessionData] = Json.writes[SessionData]

  def airToJson(airQuality: Air): JsValue = {
    Json.obj(
      "env_data" -> Json.obj(
        "temperature" -> airQuality.temperature,
        "humidity" -> airQuality.humidity,
        "particulate" -> airQuality.ppm
      )
    )
  }

  def numberOfSessionsToJson(period: String, year: Int, month: Option[Int], week: Option[Int], day: Option[Int], count: Option[Int], counts: Seq[Int]): JsValue = {
    val periodJson = period match {
      case "year" => Json.obj("year" -> year, "year_count" -> count.getOrElse(0), "monthly_count" -> counts)
      case "month" => Json.obj("year" -> year, "month" -> month.get, "month_count" -> count.getOrElse(0), "weekly_count" -> counts)
      case "week" => Json.obj("year" -> year, "month" -> month.get, "week" -> week.get, "week_count" -> count.getOrElse(0), "daily_count" -> counts)
      case "day" => Json.obj("year" -> year, "month" -> month.get, "week" -> week.get, "day" -> day.get, "day_count" -> count.getOrElse(0))
    }
    Json.obj(s"num_visits_$period" -> periodJson)
  }

  def meanExerciseTimeToJson(period: String, year: Int, month: Option[Int], week: Option[Int], day: Option[Int], mean: Double, means: Seq[Double]): JsValue = {
    val periodJson = period match {
      case "year" => Json.obj("year" -> year, "year_moy" -> mean, "monthly_moy" -> means)
      case "month" => Json.obj("year" -> year, "month" -> month.get, "month_moy" -> mean, "weekly_moy" -> means)
      case "week" => Json.obj("year" -> year, "month" -> month.get, "week" -> week.get, "week_moy" -> mean, "daily_moy" -> means)
      case "day" => Json.obj("year" -> year, "month" -> month.get, "week" -> week.get, "day" -> day.get, "day_moy" -> mean)
    }
    Json.obj(s"exo_time_$period" -> periodJson)
  }

  private def sessionToJson(session: SessionData): JsObject = {
    Json.obj(
      "sessionDuration" -> session.sessionDuration,
      "env_data" -> Json.obj(
        "temperature" -> session.envData._1,
        "humidity" -> session.envData._2,
        "particulate" -> session.envData._3
      )
    )
  }

  private def detailedSessionToJson(session: DetailedSessionData): JsObject = {
    Json.obj(
      "sessionDuration" -> session.sessionDuration,
      "env_data" -> Json.obj(
        "temperature" -> session.envData._1,
        "humidity" -> session.envData._2,
        "particulate" -> session.envData._3
      ),
      "sets" -> session.sets
    )
  }

  def sessionDataToJson(year: Int, month: Int, day: Int, dayAvg: String, sessions: Seq[SessionData]): JsValue = {
    Json.obj(
      "exo_time_day" -> Json.obj(
        "year" -> year,
        "month" -> month,
        "day" -> day,
        "day_avg" -> dayAvg,
        "sessions" -> sessions.map(sessionToJson)
      )
    )
  }

  def detailedSessionDataToJson(year: Int, month: Int, day: Int, dayAvg: String, sessions: Seq[DetailedSessionData]): JsValue = {
    Json.obj(
      "exo_time_day" -> Json.obj(
        "year" -> year,
        "month" -> month,
        "day" -> day,
        "day_avg" -> dayAvg,
        "sessions" -> sessions.map(detailedSessionToJson)
      )
    )
  }

  def sessionCountToJson(year: Int, month: Int, day: Int, sessionCount: Int, sessions: Seq[DetailedSessionData]): JsValue = {
    Json.obj(
      "year" -> year,
      "month" -> month,
      "day" -> day,
      "session_count" -> sessionCount,
      "sessions" -> sessions.map(detailedSessionToJson)
    )
  }

  case class SetData(machine: Int, weight: Float, repetitions: Option[Int], setTime: String, distances: Seq[Int], times: Seq[Float])

  case class DetailedSessionData(sessionDuration: String, envData: (Double, Double, Double), sets: Seq[SetData])

  case class SessionData(sessionDuration: String, envData: (Double, Double, Double))
}