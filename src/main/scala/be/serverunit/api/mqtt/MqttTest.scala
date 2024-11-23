package be.serverunit.api.mqtt

import akka.actor.ActorSystem
import akka.stream.alpakka.mqtt.*
import akka.stream.alpakka.mqtt.scaladsl.MqttSource
import akka.stream.scaladsl.{Keep, RestartSource, Sink}
import akka.stream.{Materializer, RestartSettings}
import .processMachine
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

object MqttTest extends App {

  // Initialize Akka system and materializer
  given ActorSystem = ActorSystem("MqttPrinterSystem")

  given Materializer = Materializer.matFromSystem

  given ExecutionContext = summon[ActorSystem].dispatcher

  // MQTT connection settings for localhost
  val connectionSettings = MqttConnectionSettings(
    "tcp://192.168.2.227:1883",
    "test-scala3-client",
    MemoryPersistence()
  )

  // Restart settings for the source
  val restartSettings = RestartSettings.apply(1.second, 10.seconds, 0.2)

  // Create MQTT source listening to each connected machine in the gym
  // Source will restart in case of failure
  val mqttSource = RestartSource.withBackoff(restartSettings) { () =>
    MqttSource.atMostOnce(
      connectionSettings.withClientId("scala3-client"),
      MqttSubscriptions(Map("salle/machine/1/data" -> MqttQoS.AtLeastOnce, "topic2" -> MqttQoS.AtLeastOnce)),
      bufferSize = 8
    )
  }

  // Run the source to process incoming messages
  val (control, future) = mqttSource
    .toMat(Sink.foreach(process))(Keep.both)
    .run()
}

def process(msg: MqttMessage): Unit = {
  // Extract the topic of the message
  println(s"Received message on topic")
  val topic = msg.topic

  topic match {
    case topic if topic.contains("machine") => {
      // Extract the end of the topic
      val machineID = topic.split("machine/").last

      // Send machine and topic to function to process the message
      processMachine(machineID, msg.payload.toArray)
    }
  }
}

/*
  val retrySource = RestartSource.withBackoff(restartSettings) { () =>
    mqttSource.map(msg => new String(msg.payload.toArray))
  }

  // Print each incoming message to the console
  retrySource
    .runWith(Sink.foreach(println))
    .onComplete(_ => {
      summon[ActorSystem].terminate()
    }) // Shut down the system after completion*/