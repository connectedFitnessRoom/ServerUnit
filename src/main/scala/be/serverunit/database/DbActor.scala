package be.serverunit.database

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import be.serverunit.actors.JsonExtractor.*
import be.serverunit.actors.MachineActor
import be.serverunit.actors.MachineActor.*
import play.api.libs.json.*

import java.time.LocalDateTime
import scala.util.matching.Regex

sealed trait DbMessage

case class MqttData(topic: String, payload: String) extends DbMessage

object DbActor {

  val actorsList = scala.collection.mutable.Map[Int, akka.actor.typed.ActorRef[MachineActor.MachineMessage]]()
  private val machinePattern: Regex = "basic_frite/machine/(\\w+)/data".r
  private val airPattern: Regex = "basic_frite/air/".r

  def apply(): Behavior[DbMessage] = Behaviors.setup(context => // Structure to store actors

    println("DbActor started")

    Behaviors.receiveMessage { // When a message is received from the MqttActor, process it
      case MqttData(topic, payload) => {
        println(s"Received message on topic $topic, I'm actor ${context.self.path.name}")

        // pattern matching on the topic
        topic match {
          case machinePattern(machine) => try {
            val jsonReceived: JsValue = Json.parse(payload)
            println(s"Received message on topic $topic, I'm actor ${context.self.path.name}") // Process the JSON as needed

            // Extract the userID from the jsonReceived
            val user = (jsonReceived \ "user").as[String]

            // Convert the machineID to an integer
            val machineID: Int = machine.toInt

            // Case match on type contained in jsonReceived
            (jsonReceived \ "type").asOpt[String] match {
              case Some("START") => // Spawn a new actor for the machine and add it to the actorsList (give it a name)
                val machineActor = context.spawn(MachineActor(machineID), s"machine-$machineID")
                actorsList += (machineID -> machineActor)

                // Send the data to the specific machine actor
                extractStartData(jsonReceived) match {
                  case Some((user, time, weight)) => machineActor ! StartData(user, time, weight)
                    Behaviors.same
                  case _ => println("Error extracting data")
                    Behaviors.same
                }
              case Some("DATA") => // Send the data to the specific machine actor
                actorsList.get(machineID) match {
                  case Some(actor) => extractData(jsonReceived) match {
                    case Some((user, distance, timer)) => // Inserting the data into the database
                      actor ! Data(distance, timer)
                      Behaviors.same
                    case _ => println("Error extracting data")
                      Behaviors.same
                  }
                  case None => println("Error: Machine actor not found")
                    Behaviors.same
                }
              case Some("END") => // Send the data to the specific machine actor
                actorsList.get(machineID) match {
                  case Some(actor) => extractEndData(jsonReceived) match {
                    case Some((user, reps, time)) => // Inserting the data into the database
                      actor ! EndData(reps, time)
                      Behaviors.same
                    case _ => println("Error extracting data")
                      Behaviors.same
                  }

                    // Terminate the actor and remove it from the actorsList
                    context.stop(actor)
                    actorsList -= machineID

                    Behaviors.same
                  case None => println("Error: Machine actor not found")
                    Behaviors.same
                }
              case None => println("Error: No type found")
                Behaviors.same
              case Some(_) => println("Error: Unknown type")
                Behaviors.same
            }
          } catch {
            case e: Exception => println(s"Error: $e")
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
    })
}



