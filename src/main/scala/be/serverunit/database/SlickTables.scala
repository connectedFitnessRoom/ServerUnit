package be.serverunit.database

import slick.jdbc.H2Profile.api.*

import java.time.Instant

object SlickTables {

  lazy val users = TableQuery[Users]
  lazy val sessions = TableQuery[UserSessions]
  lazy val sets = TableQuery[Sets]
  lazy val machines = TableQuery[Machines]
  lazy val repetitions = TableQuery[Repetitions]
  lazy val airs = TableQuery[Airs]

  class Users(tag: Tag) extends Table[User](tag, "USERS") {
    // mapping function to case class
    def * = (id, username, password).mapTo[User]

    def id = column[String]("USER_ID", O.PrimaryKey)

    def username = column[String]("USERNAME")

    def password = column[String]("PASSWORD")
  }

  class UserSessions(tag: Tag) extends Table[UserSession](tag, "SESSIONS") {
    // Mapping the case class Session to the columns
    def * = (id, userID, beginDate, endDate).mapTo[UserSession]

    def id = column[Long]("SESSION_ID", O.PrimaryKey, O.AutoInc)

    def beginDate = column[Instant]("BEGIN_DATE")

    def endDate = column[Option[Instant]]("END_DATE")

    def userID = column[String]("USER_ID")

    // Foreign key relationship
    def user = foreignKey("USER_FK", userID, users)(_.id)
  }

  class Sets(tag: Tag) extends Table[UserSet](tag, "WORKOUT_SET") {
    def * = (id, sessionID, machineID, beginDate, endDate, repetitions, weight).mapTo[UserSet]

    def id = column[Long]("SET_ID", O.AutoInc, O.PrimaryKey)

    def beginDate = column[Instant]("BEGIN_DATE")

    def endDate = column[Option[Instant]]("END_DATE")

    def repetitions = column[Option[Int]]("REPETITION")

    def weight = column[Float]("WEIGHT")

    def sessionID = column[Long]("SESSION_ID")

    def machineID = column[Int]("MACHINE_ID")

    // Foreign key
    def session = foreignKey("SESSION_FK", sessionID, sessions)(_.id)

    def machine = foreignKey("MACHINE_FK", machineID, machines)(_.machineID)

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

    def timer = column[Float]("TIMER")

    def setID = column[Long]("SET_ID")

    // Foreign key
    def set = foreignKey("SET_FK", setID, sets)(_.id)

  }

  class Airs(tag: Tag) extends Table[Air](tag, "AIR_QUALITY") {
    def * = (id, temperature, humidity, ppm, timestamp).mapTo[Air]

    def id = column[Long]("AIR_QUALITY_ID", O.PrimaryKey, O.AutoInc)

    def temperature = column[Float]("TEMPERATURE")

    def humidity = column[Float]("HUMIDITY")

    def ppm = column[Float]("PPM")

    def timestamp = column[Instant]("TIMESTAMP")
  }
}
