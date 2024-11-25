package be.serverunit.actors

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import be.serverunit.actors.MachineActor.*
import be.serverunit.api.JsonExtractor.*
import play.api.libs.json.*
import slick.jdbc.JdbcBackend.Database

import java.time.LocalDateTime
import scala.util.{Try, Success, Failure}
import scala.util.matching.Regex


object MachineManager {

  private val actorsList = scala.collection.mutable.Map[Int, akka.actor.typed.ActorRef[MachineActor.MachineMessage]]()
  private val machinePattern: Regex = "basic_frite/machine/(\\w+)/data".r
  private val airPattern: Regex = "basic_frite/air/".r

  def apply(db: Database): Behavior[processMessage] = Behaviors.setup { context =>
    println("MachineManager started")

    Behaviors.receiveMessage { case MqttMessage(topic, payload) => {
      println(s"Received message on topic $topic, I'm actor ${context.self.path.name}")

      // pattern matching on the topic
      topic match {
        case machinePattern(machine) => handleMachinePattern(payload, machine.toInt, context, db) match {
          case Success(_) => Behaviors.same
          case Failure(e) => println(s"Error handling machine pattern: $e")
            Behaviors.same
        }
        case other => println(s"Unknown topic: $other")
          Behaviors.same

        /*case airPattern() => extractAirData(jsonReceived) match {
          case Some((temperature, humidity, pm, timestamp)) =>
            // Inserting the data into the database
            //insertAirData(temperature, humidity, pm, timestamp)
            Behaviors.same
          case _ =>
            println("Error extracting data")
            Behaviors.same*/
      }
    }
    }
  }

  private def handleMachinePattern(payload: String, machineID: Int, context: ActorContext[processMessage], db: Database): Try[Unit] = {
    val jsonReceived: JsValue = Json.parse(payload)

    // Extract userID and type
    val user = (jsonReceived \ "user").asOpt[String]
    val messageType = (jsonReceived \ "type").asOpt[String]

    (user, messageType) match {
      case (Some(user), Some("START")) =>
        handleStart(jsonReceived, machineID, context, db)
      case (Some(user), Some("DATA")) =>
        handleMachineData(jsonReceived, machineID, context, db)
      case (Some(user), Some("END")) =>
        handleEndData(jsonReceived, machineID, context, db)
      case (Some(_), None) =>
        Failure(new Exception("Invalid payload format: missing 'type'"))
      case _ =>
        Failure(new Exception("Invalid payload format"))
    }
  }

  private def handleStart(jsonReceived: JsValue, machineID: Int, context: ActorContext[processMessage], db: Database): Try[Unit] = {
    actorsList.get(machineID) match {
      case Some(_) =>
        Failure(new Exception("Error: Machine actor already exists"))
      case None => extractStartData(jsonReceived) match {
        case Some((user, time, weight)) =>
          val machineActor = context.spawn(MachineActor(machineID, db), s"machineActor$machineID")
          actorsList += (machineID -> machineActor)
          machineActor ! StartData(user, time, weight)
          Success(())
        case _ =>
          Failure(new Exception("Error extracting data when handling start data"))
      }
    }
  }

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

  sealed trait processMessage

  case class MqttMessage(topic: String, payload: String) extends processMessage
}



