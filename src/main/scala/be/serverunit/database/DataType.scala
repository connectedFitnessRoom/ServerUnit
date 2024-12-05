package be.serverunit.database

import java.time.Instant

case class User(uuid: String, username: String, password: String)

case class UserSession(id: Long, userID: String, beginDate: Instant, endDate: Option[Instant])

case class Set(id: Long, sessionID: Long, machineID: Int, beginDate: Instant, endDate: Option[Instant], repetitions: Option[Int], weight: Float)

case class Machine(machineID: Int, machineName: String)

case class Repetition(setID: Long, timer: Float, distance: Int)

case class Air(id: Long, temperature: Float, humidity: Float, ppm: Float, timestamp: Instant)

