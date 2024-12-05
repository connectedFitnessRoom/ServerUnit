package be.serverunit.database.utils

import be.serverunit.database.{Air, Machine, Repetition, UserSession, Set, SlickTables, User}
import slick.jdbc.H2Profile.api.*
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object InitDatabase {
  def setupDatabase(db: Database)(implicit ec: ExecutionContext): Future[Unit] = {
    val setup = for {
      // Create the tables, including primary and foreign keys
      _ <- db.run((SlickTables.users.schema ++ SlickTables.sessions.schema ++ SlickTables.sets.schema ++ SlickTables.machines.schema ++ SlickTables.repetitions.schema ++ SlickTables.airs.schema).create)

      // Insert some users
      _ <- db.run(DBIO.seq(
        SlickTables.users += User("1", "Alice", "password"),
        SlickTables.users += User("2", "Bob", "password"),
        SlickTables.users += User("3", "Charlie", "password")
      ))

      // Insert some sessions and retrieve the auto-generated SESSION_IDs
      sessionIds <- db.run(DBIO.seq(
        SlickTables.sessions += UserSession(0, "1", java.time.Instant.now(), None),
        SlickTables.sessions += UserSession(0, "2", java.time.Instant.now(), None),
        SlickTables.sessions += UserSession(0, "3", java.time.Instant.now(), None)
      ).flatMap(_ => SlickTables.sessions.result))

      // Insert machines
      _ <- db.run(DBIO.seq(
        SlickTables.machines += Machine(0, "Machine 1"),
        SlickTables.machines += Machine(1, "Machine 2"),
        SlickTables.machines += Machine(2, "Machine 3")
      ))

      // Insert sets using the correct sessionIds
      _ <- db.run(DBIO.seq(
        SlickTables.sets += Set(0, sessionIds.head.id, 0, java.time.Instant.now(), None, None, 0),
        SlickTables.sets += Set(1, sessionIds(1).id, 1, java.time.Instant.now(), None, None, 0),
        SlickTables.sets += Set(2, sessionIds(2).id, 2, java.time.Instant.now(), None, None, 0)
      ))

      // Insert some repetitions
      _ <- db.run(DBIO.seq(
        SlickTables.repetitions += Repetition(1, 0, 0),
        SlickTables.repetitions += Repetition(2, 0, 0),
        SlickTables.repetitions += Repetition(3, 0, 0)
      ))

      // Insert some air qualities
      _ <- db.run(DBIO.seq(
        SlickTables.airs += Air(0, 0, 0, 0, java.time.Instant.now()),
        SlickTables.airs += Air(1, 3, 4, 5, java.time.Instant.now()),
        SlickTables.airs += Air(2, 6, 7, 8, java.time.Instant.now())
      ))

    } yield ()


    setup.transform {
      case Success(_) => Success(())
      case Failure(ex) => Failure(new Exception("Database setup failed", ex))
    }
  }
}

