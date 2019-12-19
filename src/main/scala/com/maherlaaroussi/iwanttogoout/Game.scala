package com.maherlaaroussi.iwanttogoout
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}

object Carte {
  def apply(): Props = Props(new Carte())
}
class Carte extends Actor with ActorLogging {

  import Map._
  val r = scala.util.Random

  // Map du jeu
  val taille = 5
  var map: Array[Array[Map[String, AnyVal]]] = Array.ofDim[Map[String, AnyVal]](taille, taille)
  for (i <- 0 until taille ; j <- 0 until taille) {
    map(i)(j) = Map(
      "monstre" -> r.nextInt(2),
      "nord" -> r.nextInt(2),
      "est" -> r.nextInt(2),
      "ouest" -> r.nextInt(2),
      "sud" -> r.nextInt(2)
    )
  }

  // TODO: Create the unique path to the exit
  // TODO: Create the list of player on the map
  // TODO: Create the class Monster
  // TODO: Receive of moving a player
  // TODO: Receive of winning the game

  def receive: Receive = {
    case msg @ _ => log.info(s"Message : $msg")
  }

}

object Player {
  def apply(): Props = Props(new Player())
}
class Player extends Actor with ActorLogging {

  // TODO: Action to fight a monster with player
  // TODO: Stats of player and get

  import Player._
  def receive: Receive = {
    case msg @ _ => log.info(s"Message : $msg")
  }

}

object main extends App {

  val systeme = ActorSystem("simplesys")
  val carte = systeme.actorOf(Carte(), "carte")
  val player = systeme.actorOf(Player(), "Maher")

}