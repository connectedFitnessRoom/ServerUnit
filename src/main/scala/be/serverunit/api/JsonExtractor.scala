package be.serverunit.api

import play.api.libs.json.*

import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDateTime, ZoneOffset}

object JsonExtractor {
  def extractStartData(json: JsValue): Option[(String, Instant, Int)] = {
    for {
      user <- (json \ "user").asOpt[String]
      time <- (json \ "time").asOpt[Instant]
      weight <- (json \ "weight").asOpt[Int]
    } yield (user, time, weight)
  }

  def extractData(json: JsValue): Option[(Int, Float)] = {
    for {
      distance <- (json \ "distance").asOpt[Int]
      timer <- (json \ "timer").asOpt[Float]
    } yield (distance, timer)
  }

  def extractEndData(json: JsValue): Option[(Int, Instant)] = {
    for {
      user <- (json \ "user").asOpt[String]
      reps <- (json \ "reps").asOpt[Int]
      time <- (json \ "time").asOpt[Instant]
    } yield (reps, time)
  }

  def extractAirData(json: JsValue): Option[(Float, Float, Float, Instant)] = {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss") // Corrected pattern
    for {
      temperature <- (json \ "temperature").asOpt[Float]
      humidity <- (json \ "humidity").asOpt[Float]
      ppm <- (json \ "ppm").asOpt[Float]
      timestamp <- (json \ "date").asOpt[String].map { dateStr =>
        LocalDateTime.parse(dateStr, formatter).toInstant(ZoneOffset.UTC)
      }
    } yield (temperature, humidity, ppm, timestamp)
  }
}
