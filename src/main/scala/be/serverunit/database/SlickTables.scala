package be.serverunit.database

import be.serverunit.database.Session
import slick.jdbc.H2Profile.api.*

import java.time.LocalDateTime

object SlickTables {

  lazy val users = TableQuery[Users]
  lazy val sessions = TableQuery[UserSessions]
  lazy val sets = TableQuery[Sets]
  lazy val machines = TableQuery[Machines]
  lazy val repetitions = TableQuery[Repetitions]
  lazy val airQualities = TableQuery[AirQualities]

  class Users(tag: Tag) extends Table[User](tag, "USERS") {
    // mapping function to case class
    def * = (id, username, password).mapTo[User]

    def id = column[String]("USER_ID", O.PrimaryKey)

    def username = column[String]("USERNAME")

    def password = column[String]("PASSWORD")
  }

  class UserSessions(tag: Tag) extends Table[Session](tag, "SESSIONS") {
    // Mapping the case class Session to the columns
    def * = (id, userID, beginDate, endDate).mapTo[Session]

    def id = column[Long]("SESSION_ID", O.PrimaryKey, O.AutoInc)

    def beginDate = column[LocalDateTime]("BEGIN_DATE")

    def endDate = column[Option[LocalDateTime]]("END_DATE")

    // Foreign key relationship
    def user = foreignKey("USER_FK", userID, users)(_.id)

    def userID = column[String]("USER_ID")
  }

  class Sets(tag: Tag) extends Table[Set](tag, "SET") {
    def * = (id, sessionID, machineID, beginDate, endDate, repetitions, weight).mapTo[Set]

    def id = column[Long]("SET_ID", O.AutoInc, O.PrimaryKey)

    def beginDate = column[LocalDateTime]("BEGIN_DATE")

    def endDate = column[Option[LocalDateTime]]("END_DATE")

    def repetitions = column[Option[Int]]("REPETITION")

    def weight = column[Float]("WEIGHT")

    // Foreign key
    def session = foreignKey("SESSION_FK", sessionID, sessions)(_.id)

    def sessionID = column[Long]("SESSION_ID")

    def machine = foreignKey("MACHINE_FK", machineID, machines)(_.machineID)

    def machineID = column[Int]("MACHINE_ID")

  }

  class Machines(tag: Tag) extends Table[Machine](tag, "MACHINES") {
    def * = (machineID, machineName).mapTo[Machine]

    def machineID = column[Int]("MACHINE_ID", O.PrimaryKey)

    def machineName = column[String]("MACHINE_NAME")
  }

  class Repetitions(tag: Tag) extends Table[Repetition](tag, "REPETITIONS") {
    def * = (setID, timer, distance).mapTo[Repetition]

    def distance = column[Int]("DISTANCE")

    // Composite primary key
    def pk = primaryKey("REP_PK", (setID, timer))

    def setID = column[Long]("SET_ID")

    def timer = column[Float]("TIMER")

    // Foreign key
    def set = foreignKey("SET_FK", setID, sets)(_.id)

  }

  class AirQualities(tag: Tag) extends Table[AirQuality](tag, "AIR_QUALITY") {
    def * = (id, temperature, humidity, pm, timestamp).mapTo[AirQuality]

    def id = column[Long]("AIR_QUALITY_ID", O.PrimaryKey)

    def temperature = column[Float]("TEMPERATURE")

    def humidity = column[Float]("HUMIDITY")

    def pm = column[Float]("PM")

    def timestamp = column[LocalDateTime]("TIMESTAMP")
  }
}
