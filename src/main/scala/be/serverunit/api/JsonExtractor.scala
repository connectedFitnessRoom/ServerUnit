package be.serverunit.api

import play.api.libs.json.*

import java.time.LocalDateTime

object JsonExtractor {
  def extractStartData(json: JsValue): Option[(String, LocalDateTime, Int)] = {
    for {
      user <- (json \ "user").asOpt[String]
      time <- (json \ "time").asOpt[LocalDateTime]
      weight <- (json \ "weight").asOpt[Int]
    } yield (user, time, weight)
  }

  def extractData(json: JsValue): Option[(Int, Float)] = {
    for {
      distance <- (json \ "distance").asOpt[Int]
      timer <- (json \ "timer").asOpt[Float]
    } yield (distance, timer)
  }

  def extractEndData(json: JsValue): Option[(Int, LocalDateTime)] = {
    for {
      user <- (json \ "user").asOpt[String]
      reps <- (json \ "reps").asOpt[Int]
      time <- (json \ "time").asOpt[LocalDateTime]
    } yield (reps, time)
  }

  def extractAirData(json: JsValue): Option[(Float, Float, Float, LocalDateTime)] = {
    for {
      temperature <- (json \ "temperature").asOpt[Float]
      humidity <- (json \ "humidity").asOpt[Float]
      pm <- (json \ "pm").asOpt[Float]
      timestamp <- (json \ "timestamp").asOpt[LocalDateTime]
    } yield (temperature, humidity, pm, timestamp)
  }
}
