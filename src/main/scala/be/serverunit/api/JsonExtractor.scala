package be.serverunit.api

import play.api.libs.json.*

import java.time.LocalDateTime

object JsonExtractor {
  def extractStartData(json: JsValue): Option[(String, LocalDateTime, Int)] = {
    // Extracting the data from the json (user, timer) using a for comprehension
    for {
      user <- (json \ "user").asOpt[String]
      time <- (json \ "time").asOpt[LocalDateTime]
      weight <- (json \ "weight").asOpt[Int]
    } yield (user, time, weight)
  }

  def extractData(json: JsValue): Option[(Int, Int)] = {
    // Extracting the data from the json (user, distance, timer) using a for comprehension
    for {
      distance <- (json \ "distance").asOpt[Int]
      timer <- (json \ "timer").asOpt[Int]
    } yield (distance, timer)
  }

  def extractEndData(json: JsValue): Option[(Int, LocalDateTime)] = {
    // Extracting the data from the json (user, reps, time) using a for comprehension
    for {
      user <- (json \ "user").asOpt[String]
      reps <- (json \ "reps").asOpt[Int]
      time <- (json \ "time").asOpt[LocalDateTime]
    } yield (reps, time)
  }

  def extractAirData(json: JsValue): Option[(Float, Float, Float, LocalDateTime)] = {
    // Extracting the data from the json (temperature, humidity, pm, timestamp) using a for comprehension
    for {
      temperature <- (json \ "temperature").asOpt[Float]
      humidity <- (json \ "humidity").asOpt[Float]
      pm <- (json \ "pm").asOpt[Float]
      timestamp <- (json \ "timestamp").asOpt[LocalDateTime]
    } yield (temperature, humidity, pm, timestamp)
  }
}
