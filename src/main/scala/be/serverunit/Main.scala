package be.serverunit

import akka.NotUsed
import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import be.serverunit.actors.{MachineManager, MqttActor}
import be.serverunit.database.{DatabaseApp, PrintDB}
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

object Main extends App {
  implicit val system: ActorSystem[NotUsed] = ActorSystem(Behaviors.empty, "serverunit")

  val db = Database.forConfig("h2mem1")

  // Set up the database by running the schema creation and inserts
  val setupFuture = DatabaseApp.setupDatabase(db)

  // Wait until the setup has been completed
  Await.result(setupFuture, Duration.Inf)
  
  // Print the contents of the database
  PrintDB.printDatabaseContents(db)

  val machineManager: ActorRef[MachineManager.processMessage] = system.systemActorOf(MachineManager(db), "machineManager")
  val mqttActor: ActorRef[MqttActor.MqttMessage] = system.systemActorOf(MqttActor(machineManager), "mqttActor")

}
