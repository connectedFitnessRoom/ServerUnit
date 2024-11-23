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

    // Insert some users
    SlickTables.users += SlickTables.User(1, "John Doe"),
    SlickTables.users += SlickTables.User(2, "Fred Smith"),
    SlickTables.users += SlickTables.User(3, "Jane Roe"),
    
    // Insert some sessions
    SlickTables.sessions += SlickTables.Session(1, 1, java.time.LocalDate.now(), java.time.LocalDate.now()),
    SlickTables.sessions += SlickTables.Session(2, 2, java.time.LocalDate.now(), java.time.LocalDate.now()),
    SlickTables.sessions += SlickTables.Session(3, 3, java.time.LocalDate.now(), java.time.LocalDate.now()),
    
    // Insert some sets
    SlickTables.sets += SlickTables.Set(1, 1, 1, java.time.LocalDate.now(), java.time.LocalDate.now(), 10, 50.0f),
    SlickTables.sets += SlickTables.Set(2, 2, 2, java.time.LocalDate.now(), java.time.LocalDate.now(), 10, 50.0f),
    SlickTables.sets += SlickTables.Set(3, 3, 3, java.time.LocalDate.now(), java.time.LocalDate.now(), 10, 50.0f),
    
    // Insert some machines
    SlickTables.machines += SlickTables.Machine(1, "Machine 1"),
    SlickTables.machines += SlickTables.Machine(2, "Machine 2"),
    SlickTables.machines += SlickTables.Machine(3, "Machine 3"),
  
    // Insert some repetitions
    SlickTables.repetitions += SlickTables.Repetition(1, 1, 10.0f, 100),
    SlickTables.repetitions += SlickTables.Repetition(2, 2, 10.0f, 100),
    SlickTables.repetitions += SlickTables.Repetition(3, 3, 10.0f, 100)
  
  )

  val setupFuture = db.run(setup)

  // Wait until the setup has been completed
  Await.result(setupFuture, scala.concurrent.duration.Duration.Inf)

  val queryFuture = db.run(SlickTables.users.result).map(_.foreach {
    case SlickTables.User(id, username) =>
      println(s"User: $id, $username")
  })

  // Wait until the query has been completed
  Await.result(queryFuture, scala.concurrent.duration.Duration.Inf)

  db.close()

}

