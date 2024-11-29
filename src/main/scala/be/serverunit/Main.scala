package be.serverunit

import akka.NotUsed
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import be.serverunit.actors.{MachineManager, MqttActor}
import be.serverunit.database.utils.{InitDatabase, PrintDB}
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

object Main extends App {
  implicit val system: ActorSystem[NotUsed] = ActorSystem(Behaviors.empty, "serverunit")

  val db = Database.forConfig("h2mem1")

  // Set up the database by running the schema creation and inserts
  val setupFuture = InitDatabase.setupDatabase(db)

  // Wait until the setup has been completed
  Await.result(setupFuture, Duration.Inf)

  // Print the contents of the database
  PrintDB.printDatabaseContents(db)

  val machineManager: ActorRef[MachineManager.processMessage] = system.systemActorOf(MachineManager(db), "machineManager")
  val mqttActor: ActorRef[MqttActor.MqttMessage] = system.systemActorOf(MqttActor(machineManager), "mqttActor")

}
