package be.serverunit.database.utils

import be.serverunit.database.{Air, Machine, Repetition, Set, SlickTables, User, UserSession}
import slick.jdbc.H2Profile.api.*
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.meta.MTable

import java.time.Instant
import java.time.temporal.ChronoUnit
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object InitDatabase {
  def setupDatabase(db: Database)(implicit ec: ExecutionContext): Future[Unit] = {
    val tables = List(
      SlickTables.users,
      SlickTables.sessions,
      SlickTables.sets,
      SlickTables.machines,
      SlickTables.repetitions,
      SlickTables.airs
    )

    val existingTables = db.run(MTable.getTables).map(_.map(_.name.name).toSet)

    existingTables.flatMap { existing =>
      val createIfNotExists = tables.filterNot(table => existing.contains(table.baseTableRow.tableName)).map(_.schema.createIfNotExists)
      val setup = DBIO.sequence(createIfNotExists).transactionally

      db.run(setup).flatMap { _ =>
        if (existing.isEmpty) {
          val insertData = DBIO.seq(
            // Insert some users
            SlickTables.users += User("1", "Alice", "password"),
            SlickTables.users += User("2", "Bob", "password"),
            SlickTables.users += User("3", "Charlie", "password"),

            // Insert some sessions and retrieve the auto-generated SESSION_IDs
            SlickTables.sessions += UserSession(0, "1", Instant.parse("2024-12-01T00:00:00Z"), None),
            SlickTables.sessions += UserSession(0, "1", Instant.parse("2024-12-02T00:00:00Z"), None),
            SlickTables.sessions += UserSession(0, "1", Instant.parse("2024-12-03T00:00:00Z"), None),
            SlickTables.sessions += UserSession(0, "2", Instant.now(), None),
            SlickTables.sessions += UserSession(0, "3", Instant.now(), None),
            // New sessions for next week
            SlickTables.sessions += UserSession(0, "1", Instant.now().plus(7, ChronoUnit.DAYS), None),
            SlickTables.sessions += UserSession(0, "1", Instant.now().plus(8, ChronoUnit.DAYS), None),
            SlickTables.sessions += UserSession(0, "1", Instant.now().plus(9, ChronoUnit.DAYS), None),

            // Insert machines
            SlickTables.machines += Machine(0, "Machine 1"),
            SlickTables.machines += Machine(1, "Machine 2"),
            SlickTables.machines += Machine(2, "Machine 3"),

            // Insert sets using the correct sessionIds
            SlickTables.sets += Set(0, 1, 0, Instant.parse("2024-12-02T00:00:00Z"), Some(Instant.parse("2024-12-02T01:00:00Z")), None, 0),
            SlickTables.sets += Set(0, 2, 0, Instant.parse("2024-12-03T00:00:00Z"), Some(Instant.parse("2024-12-03T02:00:00Z")), None, 0),
            SlickTables.sets += Set(0, 3, 0, Instant.parse("2024-12-04T00:00:00Z"), Some(Instant.parse("2024-12-04T03:00:00Z")), None, 0),
            // New sets for next week
            SlickTables.sets += Set(0, 6, 0, Instant.now().plus(7, ChronoUnit.DAYS), Some(Instant.now().plus(7, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS)), None, 0),
            SlickTables.sets += Set(0, 7, 0, Instant.now().plus(8, ChronoUnit.DAYS), Some(Instant.now().plus(8, ChronoUnit.DAYS).plus(2, ChronoUnit.HOURS)), None, 0),
            SlickTables.sets += Set(0, 8, 0, Instant.now().plus(9, ChronoUnit.DAYS), Some(Instant.now().plus(9, ChronoUnit.DAYS).plus(3, ChronoUnit.HOURS)), None, 0),

            // Sets for other users
            SlickTables.sets += Set(1, 4, 1, Instant.now(), None, None, 0),
            SlickTables.sets += Set(2, 5, 2, Instant.now(), None, None, 0),

            // Insert some repetitions
            SlickTables.repetitions += Repetition(1, 0, 0),
            SlickTables.repetitions += Repetition(2, 0, 0),
            SlickTables.repetitions += Repetition(3, 0, 0),

            // Insert some air qualities
            SlickTables.airs += Air(0, 0, 0, 0, Instant.now()),
            SlickTables.airs += Air(1, 3, 4, 5, Instant.now()),
            SlickTables.airs += Air(2, 6, 7, 8, Instant.now())
          )

          db.run(insertData).transform {
            case Success(_) => Success(())
            case Failure(ex) => Failure(new Exception("Database setup and data insertion failed", ex))
          }
        } else {
          Future.successful(())
        }
      }
    }
  }
}