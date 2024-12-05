package be.serverunit.database.utils

import java.time.Instant
import java.time.temporal.ChronoUnit

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
        SlickTables.sessions += UserSession(0, "1", Instant.parse("2024-12-01T00:00:00Z"), None),
        SlickTables.sessions += UserSession(0, "1", Instant.parse("2024-12-02T00:00:00Z"), None),
        SlickTables.sessions += UserSession(0, "1", Instant.parse("2024-12-03T00:00:00Z"), None),
        SlickTables.sessions += UserSession(0, "2", Instant.now(), None),
        SlickTables.sessions += UserSession(0, "3", Instant.now(), None),
        // New sessions for next week
        SlickTables.sessions += UserSession(0, "1", Instant.now().plus(7, ChronoUnit.DAYS), None),
        SlickTables.sessions += UserSession(0, "1", Instant.now().plus(8, ChronoUnit.DAYS), None),
        SlickTables.sessions += UserSession(0, "1", Instant.now().plus(9, ChronoUnit.DAYS), None)
      ).flatMap(_ => SlickTables.sessions.result))

      // Insert machines
      _ <- db.run(DBIO.seq(
        SlickTables.machines += Machine(0, "Machine 1"),
        SlickTables.machines += Machine(1, "Machine 2"),
        SlickTables.machines += Machine(2, "Machine 3")
      ))

      // Insert sets using the correct sessionIds
      _ <- db.run(DBIO.seq(
        // Sets for user 1 with different durations
        SlickTables.sets += Set(0, sessionIds.head.id, 0, Instant.parse("2024-12-02T00:00:00Z"), Some(Instant.parse("2024-12-02T01:00:00Z")), None, 0),
        SlickTables.sets += Set(0, sessionIds(1).id, 0, Instant.parse("2024-12-03T00:00:00Z"), Some(Instant.parse("2024-12-03T02:00:00Z")), None, 0),
        SlickTables.sets += Set(0, sessionIds(2).id, 0, Instant.parse("2024-12-04T00:00:00Z"), Some(Instant.parse("2024-12-04T03:00:00Z")), None, 0),
        // New sets for next week
        SlickTables.sets += Set(0, sessionIds(5).id, 0, Instant.now().plus(7, ChronoUnit.DAYS), Some(Instant.now().plus(7, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS)), None, 0),
        SlickTables.sets += Set(0, sessionIds(6).id, 0, Instant.now().plus(8, ChronoUnit.DAYS), Some(Instant.now().plus(8, ChronoUnit.DAYS).plus(2, ChronoUnit.HOURS)), None, 0),
        SlickTables.sets += Set(0, sessionIds(7).id, 0, Instant.now().plus(9, ChronoUnit.DAYS), Some(Instant.now().plus(9, ChronoUnit.DAYS).plus(3, ChronoUnit.HOURS)), None, 0),

        // Sets for other users
        SlickTables.sets += Set(1, sessionIds(3).id, 1, Instant.now(), None, None, 0),
        SlickTables.sets += Set(2, sessionIds(4).id, 2, Instant.now(), None, None, 0)
      ))

      // Insert some repetitions
      _ <- db.run(DBIO.seq(
        SlickTables.repetitions += Repetition(1, 0, 0),
        SlickTables.repetitions += Repetition(2, 0, 0),
        SlickTables.repetitions += Repetition(3, 0, 0)
      ))

      // Insert some air qualities
      _ <- db.run(DBIO.seq(
        SlickTables.airs += Air(0, 0, 0, 0, Instant.now()),
        SlickTables.airs += Air(1, 3, 4, 5, Instant.now()),
        SlickTables.airs += Air(2, 6, 7, 8, Instant.now())
      ))

    } yield ()

    setup.transform {
      case Success(_) => Success(())
      case Failure(ex) => Failure(new Exception("Database setup failed", ex))
    }
  }
}