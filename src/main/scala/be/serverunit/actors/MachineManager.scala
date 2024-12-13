package be.serverunit.actors

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import be.serverunit.actors.MachineActor.*
import be.serverunit.api.JsonExtractor.*
import be.serverunit.database.Air
import be.serverunit.database.operations.Basic.insertAirQuality
import org.slf4j.LoggerFactory
import play.api.libs.json.*
import slick.jdbc.JdbcBackend.Database

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.matching.Regex
import scala.util.{Failure, Success, Try} // Import the global ExecutionContext

object MachineManager {

  private val logger = LoggerFactory.getLogger(this.getClass)
  private val actorsList = scala.collection.mutable.Map[Int, akka.actor.typed.ActorRef[MachineActor.MachineMessage]]()
  
  // Define the patterns for the topics
  // The pattern for the machine data retrieve the machineID from the topic so that we can send the data to the corresponding MachineActor
  private val machinePattern: Regex = "basic_frite/machine/(\\w+)/data".r
  private val airPattern: Regex = "AetherGuard/sensordata".r

  def apply(db: Database): Behavior[processMessage] = Behaviors.setup { context =>
    logger.info("MachineManager started")

    Behaviors.receiveMessage { case MqttMessage(topic, payload) =>
      logger.info(s"Received message on topic $topic, I'm actor ${context.self.path.name}")

      // pattern matching on the topic
      topic match {
        // Use the machinePattern to extract the machineID from the topic and send the payload to the corresponding MachineActor
        case machinePattern(machine) => handleMachinePattern(payload, machine.toInt, context, db) match {
          case Success(_) => Behaviors.same
          case Failure(e) => logger.error(s"Error handling machine pattern: $e")
            Behaviors.same
        }

        case airPattern() => handleAirData(payload, context, db) match {
          case Success(_) => Behaviors.same
          case Failure(e) => logger.error(s"Error handling air data: $e")
            Behaviors.same
        }
        case other => logger.warn(s"Unknown topic: $other")
          Behaviors.same
      }
    }
  }

  private def handleMachinePattern(payload: String, machineID: Int, context: ActorContext[processMessage], db: Database): Try[Unit] = {
    val jsonReceived: Try[JsValue] = Try(Json.parse(payload))

    jsonReceived.flatMap { json =>
      (json \ "type").asOpt[String] match {
        case Some("START") => handleStart(json, machineID, context, db)
        case Some("DATA") => handleMachineData(json, machineID, context, db)
        case Some("END") => handleEndData(json, machineID, context, db)
        case None => Failure(new Exception("Invalid payload format: missing 'type'"))
        case _ => Failure(new Exception("Invalid payload type"))
      }
    }
  }

  // Create a new MachineActor and store it in the actorsList mapped to the machineID in order to retrieve and send messages to it later 
  private def handleStart(jsonReceived: JsValue, machineID: Int, context: ActorContext[processMessage], db: Database): Try[Unit] = {
    actorsList.get(machineID) match {
      case Some(_) =>
        Failure(new Exception("Error: Machine actor already exists"))
      case None => extractStartData(jsonReceived) match {
        case Some((user, time, weight)) =>
          logger.info(s"Received data: user: $user, time: $time, weight: $weight")
          val machineActor = context.spawn(MachineActor(machineID, db), s"machineActor$machineID")
          actorsList += (machineID -> machineActor)
          machineActor ! StartData(user, time, weight)
          Success(())
        case _ =>
          Failure(new Exception("Error extracting data when handling start data"))
      }
    }
  }

  // Extract the data and send it to the corresponding MachineActor by looking it up in the actorsList
  private def handleMachineData(jsonReceived: JsValue, machineID: Int, context: ActorContext[processMessage], db: Database): Try[Unit] = {
    actorsList.get(machineID) match {
      case Some(actor) => extractData(jsonReceived) match {
        case Some((distance, timer)) =>
          actor ! Data(distance, timer)
          Success(())
        case _ => Failure(new Exception("Error extracting data when handling machine data"))
      }
      case None => Failure(new Exception("Error: Machine actor not found"))
    }
  }

  // Extract the data and send it to the corresponding MachineActor by looking it up in the actorsList
  // Then stop the MachineActor and remove it from the actorsList
  private def handleEndData(jsonReceived: JsValue, machineID: Int, context: ActorContext[processMessage], db: Database): Try[Unit] = {
    actorsList.get(machineID) match {
      case Some(actor) => extractEndData(jsonReceived) match {
        case Some((reps, time)) =>
          actor ! EndData(reps, time)
          context.stop(actor)
          actorsList -= machineID
          Success(())
        case _ => Failure(new Exception("Error extracting data when handling end data"))
      }
      case None => Failure(new Exception("Error: Machine actor not found"))
    }
  }

  private def handleAirData(payload: String, context: ActorContext[processMessage], db: Database): Try[Unit] = {
    val jsonReceived: Try[JsValue] = Try(Json.parse(payload))

    jsonReceived.flatMap { json =>
      extractAirData(json) match {
        case Some((temperature, humidity, pm, timestamp)) =>
          logger.info(s"Received air data: temperature: $temperature, humidity: $humidity, pm: $pm, timestamp: $timestamp")
          val airData = Air(0, temperature, humidity, pm, timestamp)
          Try(insertAirQuality(db, airData))
        case _ =>
          Failure(new Exception("Error extracting data when handling air data"))
      }
    }
  }

  sealed trait processMessage

  case class MqttMessage(topic: String, payload: String) extends processMessage
}