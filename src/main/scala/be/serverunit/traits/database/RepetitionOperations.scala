package be.serverunit.traits.database

import be.serverunit.database.Repetition
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.Future

trait RepetitionOperations {
  def insertRepetition(db: Database, repetition: Repetition): Future[Int]
}