package be.serverunit.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.*
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import akka.stream.{Materializer, SystemMaterializer}
import be.serverunit.api.HttpFetch.*
import slick.jdbc.JdbcBackend.Database

import java.time.LocalDateTime
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object HttpActor {
  def apply(db: Database): Behavior[Command] = Behaviors.setup { context =>
    Behaviors.receiveMessage {
      case StartHttpServer =>
        startServer(context, db)
        Behaviors.same
    }
  }

  private def startServer(context: akka.actor.typed.scaladsl.ActorContext[Command], db: Database): Unit = {
    implicit val system: ActorSystem[Nothing] = context.system
    implicit val ec: ExecutionContext = system.executionContext
    implicit val materializer: Materializer = SystemMaterializer(system).materializer

    val routes: Route = concat(
      path("api" / "air_quality")(get(completeWithFetch(fetchAirQuality(db)))),
      path("api" / "number_of_sessions")(parameters("Frequency", "UserID", "Date")(handleSessionRequest(db))),
      path("api" / "mean_exercise_time")(parameters("Frequency", "UserID", "Date")(handleExerciseTimeRequest(db))),
      path("api" / "session_count")(parameters("UserID", "Date")(handleDetailedDayCountRequest(db))),
      path("api" / "session_data")(parameters("UserID", "Date")(handleDayDataRequest(db))),
      path("api" / "session_data" / "detailed")(parameters("UserID", "Date")(handleDetailedDayDataRequest(db))),
    )

    val bindingFuture = Http().newServerAt("localhost", 9000).bind(routes)
    bindingFuture.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        context.log.info(s"HTTP server started at http://${address.getHostString}:${address.getPort}/")
      case Failure(exception) =>
        context.log.error(s"Failed to bind HTTP server: ${exception.getMessage}")
        context.system.terminate()
    }
  }

  private def handleSessionRequest(db: Database)(frequency: String, userID: String, date: String)(implicit ec: ExecutionContext): Route = {
    val dateTime = LocalDateTime.parse(date)
    val future: Future[String] = frequency match {
      case "Year" => fetchNumberOfSessionsByYear(db, userID, dateTime.getYear)
      case "Month" => fetchNumberOfSessionsByMonth(db, userID, dateTime.getYear, dateTime.getMonthValue)
      case "Week" => fetchNumberOfSessionsByWeek(db, userID, dateTime.getYear, dateTime.getMonthValue, (dateTime.getDayOfMonth - 1) / 7 + 1)
      case "Day" => fetchNumberOfSessionsByDay(db, userID, dateTime.getYear, dateTime.getMonthValue, (dateTime.getDayOfMonth - 1) / 7 + 1, dateTime.getDayOfMonth)
    }
    completeWithFetch(future)
  }

  private def handleExerciseTimeRequest(db: Database)(frequency: String, userID: String, date: String)(implicit ec: ExecutionContext): Route = {
    val dateTime = LocalDateTime.parse(date)
    val future: Future[String] = frequency match {
      case "Year" => fetchMeanExerciseTimeByYear(db, userID, dateTime.getYear)
      case "Month" => fetchMeanExerciseTimeByMonth(db, userID, dateTime.getYear, dateTime.getMonthValue)
      case "Week" => fetchMeanExerciseTimeByWeek(db, userID, dateTime.getYear, dateTime.getMonthValue, (dateTime.getDayOfMonth - 1) / 7 + 1)
      case "Day" => fetchMeanExerciseTimeByDay(db, userID, dateTime.getYear, dateTime.getMonthValue, (dateTime.getDayOfMonth - 1) / 7 + 1, dateTime.getDayOfMonth)
    }
    completeWithFetch(future)
  }

  private def completeWithFetch(fetch: => Future[String])(implicit ec: ExecutionContext): Route = {
    onComplete(fetch) {
      case Success(result) => complete(HttpEntity(ContentTypes.`application/json`, result))
      case Failure(ex) => complete(StatusCodes.InternalServerError -> s"Failed: ${ex.getMessage}")
    }
  }
  
  private def handleDayDataRequest(db: Database)(userID: String, date: String)(implicit ec: ExecutionContext): Route = {
    val dateTime = LocalDateTime.parse(date)
    val future: Future[String] = fetchSessionData(db, userID, dateTime.getYear, dateTime.getMonthValue, dateTime.getDayOfMonth)
    completeWithFetch(future)
  }
  
  private def handleDetailedDayDataRequest(db: Database)(userID: String, date: String)(implicit ec: ExecutionContext): Route = {
    val dateTime = LocalDateTime.parse(date)
    val future: Future[String] = fetchDetailedSessionData(db, userID, dateTime.getYear, dateTime.getMonthValue, dateTime.getDayOfMonth)
    completeWithFetch(future)
  }
  
  private def handleDetailedDayCountRequest(db: Database)(userID: String, date: String)(implicit ec: ExecutionContext): Route = {
    val dateTime = LocalDateTime.parse(date)
    val future: Future[String] = fetchSessionCountForDay(db, userID, dateTime.getYear, dateTime.getMonthValue, dateTime.getDayOfMonth)
    completeWithFetch(future)
  }

  sealed trait Command

  case object StartHttpServer extends Command
}