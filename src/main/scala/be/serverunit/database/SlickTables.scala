package be.serverunit.database

import slick.jdbc.H2Profile.api.*

import java.time.LocalDate

object SlickTables {

  class Users(tag: Tag) extends Table[User](tag, "USERS") {
    def id = column[Long]("USER_ID", O.PrimaryKey)

    def username = column[String]("USERNAME")

    def password = column[String]("PASSWORD")

    // mapping function to case class
    def * = (id, username).mapTo[User]
  }

  lazy val users = TableQuery[Users]

  class Sessions(tag: Tag) extends Table[Session](tag, "SESSIONS") {
    def id = column[Long]("SESSION_ID", O.PrimaryKey, O.AutoInc)

    def userID = column[Long]("USER_ID")

    def beginDate = column[LocalDateTime]("BEGIN_DATE")

    // Nullable
    def endDate = column[Option[LocalDate]]("END_DATE")

    def * = (id, userID, beginDate, endDate).mapTo[Session]

    // Foreign key
    def user = foreignKey("USER_FK", userID, users)(_.id)
  }

  lazy val sessions = TableQuery[Sessions]

  class Sets(tag: Tag) extends Table[Set](tag, "SET") {
    def id = column[Int]("SET_NUMBER")

    def sessionID = column[Long]("SESSION_ID")

    def machineID = column[Int]("MACHINE_ID")

    def beginDate = column[LocalDate]("BEGIN_DATE")

    def endDate = column[Option[LocalDate]]("END_DATE")

    def repetition = column[Option[Int]]("REPETITION")

    def weight = column[Float]("WEIGHT")

    def * = (id, sessionID, machineID, beginDate, endDate, repetition, weight).mapTo[Set]

    // Composite Primary Key
    def pk = primaryKey("PK_SET", (id, sessionID))

    // Foreign key
    def session = foreignKey("SESSION_FK", sessionID, sessions)(_.id)

  }
  
  lazy val sets = TableQuery[Sets]

  class Machines(tag: Tag) extends Table[Machine](tag, "MACHINES") {
    def machineID = column[Int]("MACHINE_ID", O.PrimaryKey)

    def machineName = column[String]("MACHINE_NAME")

    def * = (machineID, machineName).mapTo[Machine]
  }

  lazy val machines = TableQuery[Machines]

  class Repetitions(tag: Tag) extends Table[Repetition](tag, "REPETITIONS") {
    def number = column[Int]("REPETITION_NUMBER")

    def setID = column[Int]("SET_ID")
    
    def sessionID = column[Long]("SESSION_ID")

    def timer = column[Float]("TIMER")

    def distance = column[Int]("DISTANCE")

    def * = (number, setID, sessionID, timer, distance).mapTo[Repetition]

    // Composite primary key
    def pk = primaryKey("PK_REPETITION", (number, setID, sessionID))

    // Foreign key
    def set = foreignKey("SET_FK", setID, sets)(_.id)
    def session = foreignKey("SESSION_FK", sessionID, sessions)(_.id)
  }

  lazy val repetitions = TableQuery[Repetitions]

  class AirQualities(tag: Tag) extends Table[AirQuality](tag, "AIR_QUALITY") {
    def id = column[Long]("AIR_QUALITY_ID", O.PrimaryKey)

    def temperature = column[Float]("TEMPERATURE")

    def humidity = column[Float]("HUMIDITY")

    def pm = column[Float]("PM")

    def timestamp = column[LocalDate]("TIMESTAMP")

    def * = (id, temperature, humidity, pm, timestamp).mapTo[AirQuality]
  }

  lazy val airQualities = TableQuery[AirQualities]
}
