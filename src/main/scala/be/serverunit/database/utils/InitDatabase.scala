package be.serverunit.database.utils

import be.serverunit.database.{Air, Machine, Repetition, UserSet, SlickTables, User, UserSession}
import slick.jdbc.H2Profile.api.*
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.meta.MTable

import java.time.Instant
import java.time.temporal.ChronoUnit
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object InitDatabase {
  def setupDatabase(db: Database)(implicit ec: ExecutionContext): Future[Unit] = {
    // Define the order of tables respecting foreign key constraints
    val tables = List(
      SlickTables.users,
      SlickTables.machines,
      SlickTables.sessions,
      SlickTables.sets,
      SlickTables.repetitions,
      SlickTables.airs
    )

    val existingTables = db.run(MTable.getTables).map(_.map(_.name.name).toSet)

    existingTables.flatMap { existing =>
      // Drop tables in reverse order to respect foreign key constraints
      val dropTables = tables.reverse.filter(table => existing.contains(table.baseTableRow.tableName)).map(_.schema.dropIfExists)
      val createTables = tables.map(_.schema.createIfNotExists)
      val setup = DBIO.sequence(dropTables ++ createTables).transactionally

      db.run(setup).flatMap { _ =>
        val insertData = DBIO.seq(
          // Insert some users
          SlickTables.users += User("1", "Alice", "password"),
          SlickTables.users += User("2", "Bob", "password"),
          SlickTables.users += User("3", "Charlie", "password"),

          // Insert some sessions and retrieve the auto-generated SESSION_IDs
          SlickTables.sessions += UserSession(0, "1", Instant.parse("2024-12-01T00:00:00Z"), Some(Instant.parse("2024-12-01T01:00:00Z"))),
          SlickTables.sessions += UserSession(0, "1", Instant.parse("2024-12-02T00:00:00Z"), Some(Instant.parse("2024-12-02T01:00:00Z"))),
          SlickTables.sessions += UserSession(0, "1", Instant.parse("2024-12-03T00:00:00Z"), Some(Instant.parse("2024-12-03T01:00:00Z"))),
          SlickTables.sessions += UserSession(0, "1", Instant.now(), None),
          SlickTables.sessions += UserSession(0, "3", Instant.now(), None),
          // New sessions for next week
          SlickTables.sessions += UserSession(0, "1", Instant.now().plus(7, ChronoUnit.DAYS), Some(Instant.now().plus(7, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS))),
          SlickTables.sessions += UserSession(0, "1", Instant.now().plus(8, ChronoUnit.DAYS), Some(Instant.now().plus(8, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS))),
          SlickTables.sessions += UserSession(0, "1", Instant.now().plus(9, ChronoUnit.DAYS), Some(Instant.now().plus(9, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS))),

          // Insert machines
          SlickTables.machines += Machine(0, "Machine 1"),
          SlickTables.machines += Machine(1, "Machine 2"),
          SlickTables.machines += Machine(2, "Machine 3"),

          // Insert sets using the correct sessionIds
          SlickTables.sets += UserSet(0, 1, 0, Instant.parse("2024-12-02T00:00:00Z"), Some(Instant.parse("2024-12-02T01:00:00Z")), None, 0),
          SlickTables.sets += UserSet(0, 1, 0, Instant.parse("2024-12-02T02:00:00Z"), Some(Instant.parse("2024-12-02T03:00:00Z")), None, 0),
          SlickTables.sets += UserSet(0, 1, 0, Instant.parse("2024-12-02T04:00:00Z"), Some(Instant.parse("2024-12-02T05:00:00Z")), None, 0),

          SlickTables.sets += UserSet(0, 2, 0, Instant.parse("2024-12-03T00:00:00Z"), Some(Instant.parse("2024-12-03T01:00:00Z")), None, 0),
          SlickTables.sets += UserSet(0, 2, 0, Instant.parse("2024-12-03T02:00:00Z"), Some(Instant.parse("2024-12-03T03:00:00Z")), None, 0),
          SlickTables.sets += UserSet(0, 2, 0, Instant.parse("2024-12-03T04:00:00Z"), Some(Instant.parse("2024-12-03T05:00:00Z")), None, 0),

          SlickTables.sets += UserSet(0, 3, 0, Instant.parse("2024-12-04T00:00:00Z"), Some(Instant.parse("2024-12-04T01:00:00Z")), None, 0),


          // New sets for next week
          SlickTables.sets += UserSet(0, 6, 0, Instant.now().plus(7, ChronoUnit.DAYS), Some(Instant.now().plus(7, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS)), None, 0),
          SlickTables.sets += UserSet(0, 7, 0, Instant.now().plus(8, ChronoUnit.DAYS), Some(Instant.now().plus(8, ChronoUnit.DAYS).plus(2, ChronoUnit.HOURS)), None, 0),
          SlickTables.sets += UserSet(0, 8, 0, Instant.now().plus(9, ChronoUnit.DAYS), Some(Instant.now().plus(9, ChronoUnit.DAYS).plus(3, ChronoUnit.HOURS)), None, 0),

          // Insert some repetitions
          SlickTables.repetitions += Repetition(1, 0, 0),
          SlickTables.repetitions += Repetition(2, 0, 0),
          SlickTables.repetitions += Repetition(3, 0, 0),
          // Repetitions for the set on December 2nd
          SlickTables.repetitions += Repetition(2, 30.0f, 100),
          SlickTables.repetitions += Repetition(2, 60.0f, 200),
          SlickTables.repetitions += Repetition(2, 90.0f, 300),

          // Insert multiple air qualities for user 1's sessions
          SlickTables.airs += Air(0, 20.0f, 50.0f, 100.0f, Instant.parse("2024-12-04T01:00:00Z")),
          SlickTables.airs += Air(1, 21.0f, 51.0f, 101.0f, Instant.parse("2024-12-04T01:00:00Z")),
          SlickTables.airs += Air(2, 22.0f, 52.0f, 102.0f, Instant.parse("2024-12-04T01:00:00Z")),
          SlickTables.airs += Air(3, 23.0f, 53.0f, 103.0f, Instant.parse("2024-12-04T01:00:00Z")),
          SlickTables.airs += Air(4, 24.0f, 54.0f, 104.0f, Instant.parse("2024-12-04T01:00:00Z")),
          SlickTables.airs += Air(5, 25.0f, 55.0f, 105.0f, Instant.parse("2024-12-02T00:45:00Z")),
          SlickTables.airs += Air(6, 26.0f, 56.0f, 106.0f, Instant.parse("2024-12-03T03:00:00Z")),
          SlickTables.airs += Air(7, 27.0f, 57.0f, 107.0f, Instant.parse("2024-12-03T03:00:00Z")),
          SlickTables.airs += Air(8, 28.0f, 58.0f, 108.0f, Instant.parse("2024-12-03T03:00:00Z"))
        )

        db.run(insertData).transform {
          case Success(_) => Success(())
          case Failure(ex) => Failure(new Exception("Database setup and data insertion failed", ex))
        }
      }
    }
  }
}