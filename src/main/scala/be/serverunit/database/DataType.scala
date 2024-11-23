package be.serverunit.database

import java.time.LocalDateTime

case class User(uuid: String, username: String, password: String)

case class Session(id: Long, userID: Long, beginDate: LocalDateTime, endDate: Option[LocalDateTime])

case class Set(id: Int, sessionID: Long, machineID: Int, beginDate: LocalDateTime, endDate: Option[LocalDateTime], repetition: Option[Int], weight: Float)

case class Machine(machineID: Int, machineName: String)

case class Repetition(number: Int, setID: Int, timer: Int, distance: Int)

case class AirQuality(id: Long, temperature: Float, humidity: Float, pm: Float, timestamp: LocalDateTime)

