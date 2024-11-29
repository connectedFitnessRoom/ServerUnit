package be.serverunit.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akka.stream.alpakka.mqtt.*
import akka.stream.alpakka.mqtt.scaladsl.MqttSource
import akka.stream.scaladsl.{Keep, RestartSource, Sink}
import akka.stream.{Materializer, RestartSettings}
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.DurationInt


object MqttActor {
  def apply(machineManager: ActorRef[MachineManager.processMessage]): Behavior[MqttMessage] = Behaviors.setup { context =>
    implicit val materializer: Materializer = Materializer(context.system)
    implicit val executionContext: ExecutionContextExecutor = context.executionContext

    val connectionSettings = MqttConnectionSettings(
      "tcp://192.168.2.227:1883",
      "test-scala3-client",
      MemoryPersistence()
    )

    // Restart settings for the source
    val restartSettings = RestartSettings.apply(1.second, 10.seconds, 0.2)

    val mqttSource = RestartSource.withBackoff(restartSettings) { () =>
      MqttSource.atMostOnce(
        connectionSettings.withClientId("scala3-client"),
        MqttSubscriptions(Map("basic_frite/machine/#" -> MqttQoS.AtLeastOnce, "AetherGuard/sensordata" -> MqttQoS.AtLeastOnce)),
        bufferSize = 8
      )
    }

    // Run the MQTT source
    val (control, future) = mqttSource
      .map { mqttMessage =>
        // Transform the Alpakka MqttMessage into MqttActor.MqttMessage
        MqttData(mqttMessage.topic, mqttMessage.payload.utf8String)
      }
      .toMat(Sink.foreach { mqttData =>
        context.self ! mqttData // Send the transformed message to itself
      })(Keep.both)
      .run()


    Behaviors.receiveMessage {
      case MqttData(topic, payload) =>
        machineManager ! MachineManager.MqttMessage(topic, payload)
        Behaviors.same
    }
  }

  sealed trait MqttMessage

  private case class MqttData(topic: String, payload: String) extends MqttMessage
}
