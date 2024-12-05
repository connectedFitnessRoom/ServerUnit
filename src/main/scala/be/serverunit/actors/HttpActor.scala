package be.serverunit.actors

import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives.*
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.*
import akka.stream.{Materializer, SystemMaterializer}
import be.serverunit.api.HttpFetch.*
import be.serverunit.api.JsonConvertor.convertAirQualityToJson
import be.serverunit.database.Air
import be.serverunit.database.operations.Query.*
import slick.jdbc.JdbcBackend.Database

import java.time.LocalDateTime
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}


object HttpActor {
  sealed trait Command
  case object StartHttpServer extends Command

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

    // Define the routes
    val routes: Route = concat(
      path("api" / "air_quality") {
        get {
          onComplete(fetchAirQuality(db)) {
            // On success, return the result as a json string
            case Success(result) => complete(HttpEntity(ContentTypes.`application/json`, result))
            case Failure(ex) => complete(StatusCodes.InternalServerError -> s"Failed: ${ex.getMessage}")
          }
        }
      },
      path("api" / "number_of_sessions") {
        parameters("Frequency", "UserID", "Date") { (frequency, userID, date) =>
          // Parse the date string into a LocalDateTime object
          val dateTime = LocalDateTime.parse(date)
          // Fetch the number of sessions for a user on a given date
          val future: Future[String] = frequency match {
            case "Year" => fetchNumberOfSessionsByYear(db, userID, dateTime.getYear)
            case "Month" => fetchNumberOfSessionsByMonth(db, userID, dateTime.getYear, dateTime.getMonthValue)
            case "Week" => fetchNumberOfSessionsByWeek(db, userID, dateTime.getYear, dateTime.getMonthValue, {dateTime.getDayOfMonth - 1} / 7 + 1)
          }
          onComplete(future) {
            // On success, return the result
            case Success(result) => complete(result)
            case Failure(ex) => complete(StatusCodes.InternalServerError -> s"Failed: ${ex.getMessage}")
          }
        }
      },
      path("api" / "mean_exercise_time") {
        parameters("Frequency", "UserID", "Date") { (frequency, userID, date) =>
          // Parse the date string into a LocalDateTime object
          val dateTime = LocalDateTime.parse(date)
          // Fetch the mean exercise time for a user on a given date
          val future: Future[String] = frequency match {
            case "Year" => fetchMeanExerciseTimeByYear(db, userID, dateTime.getYear)
            case "Month" => fetchMeanExerciseTimeByMonth(db, userID, dateTime.getYear, dateTime.getMonthValue)
            case "Week" => fetchMeanExerciseTimeByWeek(db, userID, dateTime.getYear, dateTime.getMonthValue, {dateTime.getDayOfMonth - 1} / 7 + 1)
          }
          onComplete(future) {
            // On success, return the result
            case Success(result) => complete(result)
            case Failure(ex) => complete(StatusCodes.InternalServerError -> s"Failed: ${ex.getMessage}")
          }
        }
      }
    )

    // Start the HTTP server
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

  private def fetchFromDatabase(db: Database, api: String): scala.concurrent.Future[String] = {
    // Example query: Fetch data from a hypothetical "data_table" where "api" matches the path
    println(s"Fetching data for $api")
    scala.concurrent.Future.successful(s"Data for $api")
  }

}