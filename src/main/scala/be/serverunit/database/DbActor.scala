package be.serverunit.database

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import be.serverunit.actors.JsonExtractor.*
import play.api.libs.json.*

import scala.util.matching.Regex

sealed trait DbMessage

case class StartData(user: String, timer: Int) extends DbMessage

case class Data(user: String, distance: Int, timer: Int) extends DbMessage

case class EndData(user: String, reps: Int, time: Int) extends DbMessage

case class MqttData(topic: String, payload: String) extends DbMessage

object DbActor {

  private val machinePattern: Regex = "basic_frite/machine/(\\w+)/data".r
  private val airPattern: Regex = "basic_frite/air/".r

  def apply(): Behavior[DbMessage] =
    Behaviors.setup(context =>
      // Structure to store actors
      val actorsList = scala.collection.mutable.Map[Int, ActorRef[DbMessage]]()

      Behaviors.receiveMessage {
        // When a message is received from the MqttActor, process it
        case MqttData(topic, payload) => {
          val json: JsValue = Json.parse(payload)

          // pattern matching on the topic
          topic match {
            case machinePattern(machineId) =>
              // Extract the userID from the json
              val user = (json \ "user").as[String]

              // Convert the machineId to an integer
              val machineId = machineId.toInt

              // Case match on type contained in json
              json \ "type" match {
                case Some("START") =>
                  // Spawn a new actor for the machine and add it to the actorsList (give it a name)
                  val machineActor = context.spawn(MachineActor(), "machine-" + machineId)
                  actorsList += (machineId -> machineActor)

                  // Send the data to the specific machine actor
                  extractStartData(json) match {
                    case Some((user, time, weight)) =>
                      machineActor ! StartData(user, time, weight)
                      Behaviors.same
                    case _ =>
                      println("Error extracting data")
                      Behaviors.same
                  }

                case Some("DATA") =>
                  // Send the data to the specific machine actor
                  actorsList.get(machineId) match {
                    case Some(actor) =>
                      extractData(json) match {
                        case Some((user, distance, timer)) =>
                          // Inserting the data into the database
                          actor ! Data(user, distance, timer)
                          Behaviors.same
                        case _ =>
                          println("Error extracting data")
                          Behaviors.same
                      }
                    case None =>
                      println("Error: Machine actor not found")
                      Behaviors.same
                  }

                case Some("END") =>
                  // Send the data to the specific machine actor
                  actorsList.get(machineId) match {
                    case Some(actor) =>
                      extractEndData(json) match {
                        case Some((user, reps, time)) =>
                          // Inserting the data into the database
                          actor ! EndData(user, reps, time)
                          Behaviors.same
                        case _ =>
                          println("Error extracting data")
                          Behaviors.same
                      }

                      // Terminate the actor and remove it from the actorsList
                      context.stop(actor)
                      actorsList -= machineId

                    case None =>
                      println("Error: Machine actor not found")
                      Behaviors.same
                  }
              }

            case airPattern() => extractAirData(json) match {
              case Some((temperature, humidity, pm, timestamp)) =>
                // Inserting the data into the database
                insertAirData(temperature, humidity, pm, timestamp)
                Behaviors.same
              case _ =>
                println("Error extracting data")
                Behaviors.same
            }

            case _ =>
              println("Error extracting data")
              Behaviors.same
          }
        }
      }
    )
}



