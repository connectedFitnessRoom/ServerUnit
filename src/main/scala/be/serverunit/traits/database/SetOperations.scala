package be.serverunit.traits.database

import be.serverunit.database.Set
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.{ExecutionContext, Future}

trait SetOperations {
  def insertSetWithReturn(db: Database, set: Set)(implicit ec: ExecutionContext): Future[Set]

  def updateSet(db: Database, set: Set): Future[Int]
}
