package be.serverunit.database

import java.time.LocalDateTime

case class User(uuid: Long, username: String, password: String)

case class Session(id: Long, userID: Long, beginDate: LocalDateTime, endDate: LocalDateTime)

case class Set(id: Int, sessionID: Long, machineID: Int, beginDate: LocalDateTime, endDate: LocalDateTime, repetition: Int, weight: Float)

case class Machine(machineID: Int, machineName: String)

case class Repetition(number: Int, setID: Int, timer: Float, distance: Int)

case class AirQuality(id: Long, temperature: Float, humidity: Float, pm: Float, timestamp: LocalDateTime)

