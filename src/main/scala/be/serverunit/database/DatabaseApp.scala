package be.serverunit.database

// Use H2Profile to connect to an H2 database

import slick.jdbc.H2Profile.api.*

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

object DatabaseApp extends App {
  val db = Database.forConfig("h2mem1")

  val setup = DBIO.seq(
    // Create the tables, including primary and foreign keys
    (SlickTables.users.schema ++ SlickTables.sessions.schema ++ SlickTables.sets.schema ++ SlickTables.machines.schema ++ SlickTables.repetitions.schema).create,

    SlickTables.users += User("1", "Alice", "password"),
    SlickTables.users += User("2", "Bob", "password"),
    SlickTables.users += User("3", "Charlie", "password"),

    // Insert some sessions (id, userID, beginDate, endDate)
    SlickTables.sessions += Session(0, 1, java.time.LocalDateTime.now(), None),
    SlickTables.sessions += Session(1, 2, java.time.LocalDateTime.now(), None),
    SlickTables.sessions += Session(2, 3, java.time.LocalDateTime.now(), None),

    // Insert some sets (id, sessionID, machineID, beginDate, endDate, repetition, weight)
    SlickTables.sets += Set(0, 0, 0, java.time.LocalDateTime.now(), None, None, 0),
    SlickTables.sets += Set(1, 1, 1, java.time.LocalDateTime.now(), None, None, 0),
    SlickTables.sets += Set(2, 2, 2, java.time.LocalDateTime.now(), None, None, 0),

    // Insert some machines (machineID, machineName)
    SlickTables.machines += Machine(0, "Machine 1"),
    SlickTables.machines += Machine(1, "Machine 2"),
    SlickTables.machines += Machine(2, "Machine 3"),

    // Insert some repetitions (number, setID, timer, distance)
    SlickTables.repetitions += Repetition(0, 0, 0, 0),
    SlickTables.repetitions += Repetition(1, 1, 1, 1),
    SlickTables.repetitions += Repetition(2, 2, 2, 2)
  
  )

  val setupFuture = db.run(setup)

  // Wait until the setup has been completed
  Await.result(setupFuture, scala.concurrent.duration.Duration.Inf)

  db.close()

}

