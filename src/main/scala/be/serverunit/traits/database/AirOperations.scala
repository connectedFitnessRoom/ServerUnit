package be.serverunit.traits.database

import be.serverunit.database.Air
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.{ExecutionContext, Future}

trait AirOperations {
  def insertAirQuality(db: Database, air: Air)(implicit ec: ExecutionContext): Future[Int]
}
