package be.serverunit.traits.database

import be.serverunit.database.UserSet
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.{ExecutionContext, Future}

trait SetOperations {
  def insertSetWithReturn(db: Database, set: UserSet)(implicit ec: ExecutionContext): Future[UserSet]

  def updateASet(db: Database, set: UserSet)(implicit ec: ExecutionContext): Future[Int]
}
