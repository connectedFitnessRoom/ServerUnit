package be.serverunit.database

import java.time.LocalDateTime

case class User(uuid: String, username: String, password: String)

case class Session(id: Long, userID: String, beginDate: LocalDateTime, endDate: Option[LocalDateTime])

case class Set(id: Long, sessionID: Long, machineID: Int, beginDate: LocalDateTime, endDate: Option[LocalDateTime], repetitions: Option[Int], weight: Float)

case class Machine(machineID: Int, machineName: String)

case class Repetition(setID: Long, timer: Float, distance: Int)

case class AirQuality(id: Long, temperature: Float, humidity: Float, pm: Float, timestamp: LocalDateTime)

