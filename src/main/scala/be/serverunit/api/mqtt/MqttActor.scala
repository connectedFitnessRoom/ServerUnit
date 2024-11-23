package be.serverunit.api.mqtt

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akka.stream.alpakka.mqtt.*
import akka.stream.alpakka.mqtt.scaladsl.MqttSource
import akka.stream.scaladsl.{Keep, RestartSource, Sink}
import akka.stream.{Materializer, RestartSettings}
import be.serverunit.database.MqttData
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.DurationInt


object MqttActor {

  def apply(dbActor: ActorRef[MqttData]): Behavior[MqttMessage] = Behaviors.setup { context =>
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
        MqttSubscriptions(Map("bfscalabackend/machine/#" -> MqttQoS.AtLeastOnce, "topic2" -> MqttQoS.AtLeastOnce)),
        bufferSize = 8
      )
    }

    val (control, future) = mqttSource
      .toMat(Sink.foreach(message => context.self ! message))(Keep.both)
      .run()


    Behaviors.receiveMessage {
      // When a message is received from the MQTT source, forward it to the DbActor
      case message: MqttMessage =>
        val decodedPayload = new String(message.payload.toArray, "UTF-8")
        dbActor ! MqttData(message.topic, decodedPayload)
        Behaviors.same
    }
  }
}
