package com.maherlaaroussi.iwanttogoout
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, DeadLetter, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor}

object Game {
  case class NewPlayer(player: ActorRef)
  case class AttackPlayer(player: ActorRef)
  case class PositionJoueur(player: ActorRef)
  case object GenerateMap
  case class MoveJoueur(player: ActorRef, direction: String)
  def apply(): Props = Props(new Game())
}
class Game extends Actor with ActorLogging {

  import Game._

  val r = scala.util.Random
  val taille = 6
  var map: Array[Array[Map[String, AnyVal]]] = Array.ofDim[Map[String, AnyVal]](taille, taille)
  var players: Map[ActorRef, (Int, Int)] = Map[ActorRef, (Int, Int)]()
  var dead_players: Set[ActorRef] = Set[ActorRef]()
  implicit val timeout: Timeout = new Timeout(5 seconds)
  implicit val executionContext: ExecutionContextExecutor = ActorSystem().dispatcher

  def generateMap(): Unit = {
    // ----- Map aléatoire du jeu
    for (i <- 0 until taille ; j <- 0 until taille) {
      map(i)(j) = Map(
        "monstre" -> r.nextInt(2),
        "nord" -> r.nextInt(2),
        "est" -> r.nextInt(2),
        "ouest" -> r.nextInt(2),
        "sud" -> r.nextInt(2)
      ).withDefaultValue(0)
    }
    // ----- Création du chemin de sortie
    // Il part du centre de la carte et finit tout à gauche
    for (i <- 0 until taille/2) {
      map(i)(taille/2) = map(i)(taille/2) + ("est" -> 1)
    }
  }

  def findPlayer(player: ActorRef): Option[(ActorRef, (Int, Int))] = {
    return players.find(_._1 == player)
  }

  def sayonaraPlayer(player: ActorRef): Unit = {
    dead_players = dead_players + players.find(_._1 == player).get._1
    players = players - player
    log.info(player.path.name + " est mort :/ !")
  }

  def positionJoueur(player: ActorRef): Option[(Int, Int)] = {
    findPlayer(player) match {
      case Some(j) => return Option(j._2)
      case None => return None
    }
  }

  // TODO: Receive of winning the game

  def receive: Receive = {
    case GenerateMap => generateMap()
    case NewPlayer(player) => players += (player -> (taille/2, taille/2))
    case AttackPlayer(player) => findPlayer(player) match {
      case Some(j) => j._1 ! Player.Damage(1 + r.nextInt(100))
      case None => ;
    }
    case PositionJoueur(player) => findPlayer(player) match {
      case Some(j) => log.info(j._1.path.name + ": " + j._2)
      case None => ;
    }
    case MoveJoueur(player, direction) =>
      log.info(player.path.name + " -> " + direction)
      var inci = Map("est" -> 1, "ouest" -> -1).withDefaultValue(0)(direction)
      var incj = Map("nord" -> -1, "sud" -> 1).withDefaultValue(0)(direction)
      var dgts = 1 + r.nextInt(100)
      var here = false
      players map { j =>
        if (j._1 == player) {
          here = true
          var posx = j._2._1 + inci
          var posy = j._2._2 + incj
          if (posx > taille/2 || posx < 0) posx = j._2._1
          if (posy > taille/2 || posy < 0) posy = j._2._2
          players = players + (j._1 -> (posx, posy))
          // En présence d'un monstre
          if (map(posx)(posy)("monstre") == 1) {
            val resFuture = (player ? Player.GetLife).mapTo[Int]
            var life = Await.result(resFuture, 10 seconds)
            j._1 ! Player.Damage(dgts)
            val resFutureLife = (player ? Player.GetLife).mapTo[Int]
            life = Await.result(resFutureLife, 5 seconds)
            log.info(player.path.name + ": -" + dgts + ", Life: " + life)
            if (life == 0) {
              sayonaraPlayer(player)
            }
          }
        }
      if (!here) log.info(player.path.name + " n'est plus de ce monde !")
     }
    case msg @ _ => log.info(s"Message : $msg")
  }

}

object Player {
  case class Damage(value: Int)
  case object GetLife
  def apply(): Props = Props(new Player())
}
class Player extends Actor with ActorLogging {

  import Player._
  import Game._

  var life = 100
  val currSender: ActorRef = sender()
  implicit val timeout: Timeout = new Timeout(5 seconds)

  def getLife(): Unit = {
    val client = sender()
    client ! life
  }

  def receive: Receive = {
    case Damage(value) =>
      life -= value
      if (life < 0 || life == 0) {
        life = 0
      }
    case GetLife =>
      val game = sender
      game ! getLife()
    case msg @ _ => log.info(s"Message : $msg")
  }

}

object main extends App {

  import Game._
  import Player._

  val systeme = ActorSystem("simplesys")
  val carte = systeme.actorOf(Game(), "carte")
  val maher = systeme.actorOf(Player(), "Maher")
  val john = systeme.actorOf(Player(), "John")

  carte ! GenerateMap

  carte ! NewPlayer(maher)
  Thread.sleep(1000)
  carte ! NewPlayer(john)
  Thread.sleep(1000)

  carte ! PositionJoueur(maher)
  Thread.sleep(1000)
  carte ! PositionJoueur(maher)
  Thread.sleep(1000)

  move(maher, "nord")
  move(john, "ouest")
  move(maher, "ouest")
  move(john, "nord")

  def move(j: ActorRef, d: String): Unit = {
    carte ! MoveJoueur(j, d)
    Thread.sleep(500)
    carte ! PositionJoueur(j)
    Thread.sleep(1000)
  }

  System.exit(0)

}