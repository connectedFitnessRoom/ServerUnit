package be.serverunit.database

import slick.jdbc.H2Profile.api.*
import slick.jdbc.JdbcBackend.Database
import be.serverunit.database.Session

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

object PrintDB {
  def printDatabaseContents(db: Database): Unit = {
    // Query all tables and print their contents
    val usersFuture: Future[Seq[User]] = db.run(SlickTables.users.result)
    val sessionsFuture: Future[Seq[Session]] = db.run(SlickTables.sessions.result)
    val setsFuture: Future[Seq[Set]] = db.run(SlickTables.sets.result)
    val machinesFuture: Future[Seq[Machine]] = db.run(SlickTables.machines.result)
    val repetitionsFuture: Future[Seq[Repetition]] = db.run(SlickTables.repetitions.result)

    // Print all the results once they're retrieved
    for {
      users <- usersFuture
      sessions <- sessionsFuture
      sets <- setsFuture
      machines <- machinesFuture
      repetitions <- repetitionsFuture
    } yield {
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
    }

    // Block until all queries complete
    Await.result(Future.sequence(Seq(usersFuture, sessionsFuture, setsFuture, machinesFuture, repetitionsFuture)), Duration.Inf)
  }
}
