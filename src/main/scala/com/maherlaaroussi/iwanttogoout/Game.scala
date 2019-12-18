package com.maherlaaroussi.iwanttogoout
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}

object Map {
  def apply(): Props = Props(new Map())
}
class Map extends Actor with ActorLogging {
  import Map._
  def receive: Receive = {
    case msg @ _ => log.info(s"Message : $msg")
  }
}

object Player {
  def apply(): Props = Props(new Player())
}
class Player extends Actor with ActorLogging {
  import Player._
  def receive: Receive = {
    case msg @ _ => log.info(s"Message : $msg")
  }
}

object main extends App {

  val systeme = ActorSystem("simplesys")
  val map = systeme.actorOf(Map(), "map")
  val player = systeme.actorOf(Player(), "Maher")

}