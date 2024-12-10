package be.serverunit.traits.database

import be.serverunit.database.UserSession
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.{ExecutionContext, Future}

trait SessionOperations {
  def getLastSessionByUser(db: Database, userID: String)(implicit ec: ExecutionContext): Future[Option[UserSession]]
}
