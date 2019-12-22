package com.maherlaaroussi.iwanttogoout
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.ExecutionContext

object Game {
  case class NewPlayer(player: ActorRef)
  case class AttackPlayer(player: ActorRef)
  case class PositionJoueur(player: ActorRef)
  case class MoveJoueur(player: ActorRef, direction: String)
  def apply(): Props = Props(new Game())
}
class Game extends Actor with ActorLogging {

  import Game._

  val r = scala.util.Random
  val taille = 6
  var map: Array[Array[Map[String, AnyVal]]] = Array.ofDim[Map[String, AnyVal]](taille, taille)
  var players: Map[ActorRef, (Int, Int)] = Map[ActorRef, (Int, Int)]()
  implicit val timeout = new Timeout(2 seconds)
  implicit val executionContext = ActorSystem().dispatcher

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

  def chercherJoueur(player: ActorRef): Option[(ActorRef, (Int, Int))] = {
    return Option(players.find(_._1 == player).get)
  }

  // TODO: Create the class Monster
  // TODO: Receive of winning the game

  def receive: Receive = {
    case NewPlayer(player) => players += (player -> (taille/2, taille/2))
    case AttackPlayer(player) => chercherJoueur(player) match {
      case Some(j) => j._1 ! Player.Degats(1 + r.nextInt(100))
      case None => log.info("Ce joueur n'est pas dans la carte !")
    }
    case PositionJoueur(player) => chercherJoueur(player) match {
      case Some(j) => log.info(j._1.path.name + ": " + j._2)
      case None => log.info("Ce joueur n'est pas dans la carte !")
    }
    case MoveJoueur(player, direction) =>
      var inci = Map("est" -> 1, "ouest" -> -1).withDefaultValue(0)(direction)
      var incj = Map("nord" -> -1, "sud" -> 1).withDefaultValue(0)(direction)
      var dgts = 1 + r.nextInt(100)
      players map { j =>
        if (j._1 == player) {
          players = players + (j._1 -> (j._2._1 + inci, j._2._2 + incj))
          // En présence d'un monstre
          if (map(j._2._1 + inci)(j._2._2 + incj)("monstre") == 1) {
            log.info(player.path.name + " a subi " + dgts + " dégats")
            j._1 ! Player.Degats(dgts)
          }
        }
     }
    case msg @ _ => log.info(s"Message : $msg")
  }

}

object Player {
  case class Degats(valeur: Int)
  case object Stats
  def apply(): Props = Props(new Player())
}
class Player extends Actor with ActorLogging {

  // TODO: Action to fight a monster with player

  import Player._

  var life = 100

  def receive: Receive = {
    case Degats(valeur) =>
      life -= valeur
      if (life < 0) life = 0
      log.info(self.path.name + ": " + life)
    case Stats =>
      val stats = Map(
        "name" -> self.path.name,
        "life" -> life
      )
      sender ! stats
    case msg @ _ => log.info(s"Message : $msg")
  }

}

object main extends App {

  import Game._
  import Player._

  val systeme = ActorSystem("simplesys")
  val carte = systeme.actorOf(Game(), "carte")
  val player = systeme.actorOf(Player(), "Maher")
  carte ! NewPlayer(player)
  Thread.sleep(1000)
  carte ! PositionJoueur(player)
  Thread.sleep(1000)
  carte ! MoveJoueur(player, "ouest")
  Thread.sleep(1000)
  carte ! PositionJoueur(player)
  Thread.sleep(1000)
  carte ! MoveJoueur(player, "ouest")
  Thread.sleep(1000)
  carte ! PositionJoueur(player)
  Thread.sleep(1000)
  carte ! MoveJoueur(player, "ouest")
  Thread.sleep(1000)
  carte ! PositionJoueur(player)

}