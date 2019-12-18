package com.maherlaaroussi.iwanttogoout
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}

object Map {
  def apply(): Props = Props(new Map())
}
class Map extends Actor with ActorLogging {
  var messages = List()
  log.info("Generating map ...")
  def receive: Receive = {
    case msg @ _ => log.info(s"Message : $msg")
  }
}

object Player {
  def apply(): Props = Props(new Player())
  case class Message(msg: String)
}
class Player extends Actor with ActorLogging {
  import Player._
  def receive: Receive = {
    case Message(msg) => log.info("Player: " + msg)
    case msg @ _ => log.info(s"Message : $msg")
  }
}

object main extends App {

  val systeme = ActorSystem("simplesys")
  val map = systeme.actorOf(Map(), "map")
  val player = systeme.actorOf(Player(), "Maher")
  //player ! Player.Message("Eh hop je rejoins la partie !")

}