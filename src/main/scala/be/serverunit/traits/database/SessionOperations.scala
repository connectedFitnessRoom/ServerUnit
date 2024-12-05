package be.serverunit.traits.database

import be.serverunit.database.UserSession
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.Future

trait SessionOperations {
  def getLastSessionByUser(db: Database, userID: String): Future[Option[UserSession]]
}
