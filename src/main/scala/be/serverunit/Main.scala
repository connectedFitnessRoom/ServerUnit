import akka.actor.typed.scaladsl.adapter.*
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.{Cancellable, CoordinatedShutdown, Scheduler}
import akka.NotUsed
import be.serverunit.actors.{HttpActor, MachineManager, MqttActor}
import be.serverunit.database.utils.{InitDatabase, PrintDB}
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.*
import scala.concurrent.Await
import scala.io.StdIn

object Main extends App {
  implicit val system: ActorSystem[NotUsed] = ActorSystem(Behaviors.empty, "serverunit")
  implicit val scheduler: Scheduler = system.scheduler.toClassic

  val db = Database.forConfig("h2mem1")

  // Set up the database by running the schema creation and inserts
  val setupFuture = InitDatabase.setupDatabase(db)

  // Wait until the setup has been completed
  Await.result(setupFuture, Duration.Inf)

  val machineManager: ActorRef[MachineManager.processMessage] = system.systemActorOf(MachineManager(db), "machineManager")
  val mqttActor: ActorRef[MqttActor.MqttMessage] = system.systemActorOf(MqttActor(machineManager), "mqttActor")
  val httpActor: ActorRef[HttpActor.Command] = system.systemActorOf(HttpActor(db), "httpActor")

  httpActor ! HttpActor.StartHttpServer

  // Add a shutdown hook to gracefully terminate the actor system
  CoordinatedShutdown(system).addJvmShutdownHook {
    system.terminate()
    Await.result(system.whenTerminated, Duration.Inf)
  }

  // Schedule periodic printing of the database contents
  val cancellable: Cancellable = system.scheduler.scheduleAtFixedRate(
    initialDelay = 0.seconds,  // Start immediately
    interval = 20.seconds      // Repeat every 20 seconds
  )(() => PrintDB.printDatabaseContents(db))

  // Keep the application running until user presses return
  println("Press RETURN to stop...")
  StdIn.readLine()

  // Cancel the periodic task when shutting down
  cancellable.cancel()
  system.terminate()
}
