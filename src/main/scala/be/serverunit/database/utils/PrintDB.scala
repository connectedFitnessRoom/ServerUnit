package be.serverunit.database.utils

import be.serverunit.database.{Air, Machine, Repetition, Set, SlickTables, User, UserSession}
import slick.jdbc.H2Profile.api.*
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object PrintDB {
  // Maintain previous state for comparison
  private var previousUsers: Seq[User] = Seq.empty
  private var previousSessions: Seq[UserSession] = Seq.empty
  private var previousSets: Seq[Set] = Seq.empty
  private var previousMachines: Seq[Machine] = Seq.empty
  private var previousRepetitions: Seq[Repetition] = Seq.empty
  private var previousAirs: Seq[Air] = Seq.empty

  def printDatabaseContents(db: Database): Unit = {
    // Query all tables
    val usersFuture: Future[Seq[User]] = db.run(SlickTables.users.result)
    val sessionsFuture: Future[Seq[UserSession]] = db.run(SlickTables.sessions.result)
    val setsFuture: Future[Seq[Set]] = db.run(SlickTables.sets.result)
    val machinesFuture: Future[Seq[Machine]] = db.run(SlickTables.machines.result)
    val repetitionsFuture: Future[Seq[Repetition]] = db.run(SlickTables.repetitions.result)
    val airQualitiesFuture: Future[Seq[Air]] = db.run(SlickTables.airs.result)

    // Process all data and log new entries
    for {
      users <- usersFuture
      sessions <- sessionsFuture
      sets <- setsFuture
      machines <- machinesFuture
      repetitions <- repetitionsFuture
      airs <- airQualitiesFuture
    } yield {
      // Log full database contents
      println("Users:")
      users.foreach(println)
      println("Sessions:")
      sessions.foreach(println)
      println("Sets:")
      sets.foreach(println)
      println("Machines:")
      machines.foreach(println)
      println("Repetitions:")
      repetitions.foreach(println)
      println("Air Qualities:")
      airs.foreach(println)

      // Log newly inserted records
      logNewRecords("Users", previousUsers, users)
      logNewRecords("Sessions", previousSessions, sessions)
      logNewRecords("Sets", previousSets, sets)
      logNewRecords("Machines", previousMachines, machines)
      logNewRecords("Repetitions", previousRepetitions, repetitions)
      logNewRecords("Air Qualities", previousAirs, airs)

      // Update previous state
      previousUsers = users
      previousSessions = sessions
      previousSets = sets
      previousMachines = machines
      previousRepetitions = repetitions
      previousAirs = airs
    }

    // Block until all queries complete
    Await.result(Future.sequence(Seq(usersFuture, sessionsFuture, setsFuture, machinesFuture, repetitionsFuture, airQualitiesFuture)), Duration.Inf)
  }

  // Generic method to log new records
  private def logNewRecords[T](tableName: String, previousRecords: Seq[T], currentRecords: Seq[T]): Unit = {
    val newRecords = currentRecords.diff(previousRecords) // Find new records
    if (newRecords.nonEmpty) {
      println(s"New records in $tableName:")
      newRecords.foreach(println)
    } else {
      println(s"No new records in $tableName.")
    }
  }
}
