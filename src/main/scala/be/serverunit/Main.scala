package be.serverunit

import akka.NotUsed
import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import be.serverunit.api.mqtt.MqttActor
import be.serverunit.database.DbActor

object Main {
  def apply(): Behavior[NotUsed] =
    Behaviors.setup {context =>
    val dbActor = context.spawn (DbActor(), "db-actor")
    val mqttActor = context.spawn (MqttActor(dbActor), "mqtt-actor")
    //val httpActor = context.spawn (HttpActor(), "http-actor")
    
    Behaviors.receiveSignal {
      case (_, _) => Behaviors.stopped
    }
    
    }
  
  def main(args: Array[String]): Unit = {
    ActorSystem(Main(), "BFScalaBackend")
  }
}
