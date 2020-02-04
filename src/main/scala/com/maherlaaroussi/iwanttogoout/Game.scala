package com.maherlaaroussi.iwanttogoout
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
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
  implicit val timeout: Timeout = new Timeout(2 seconds)
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

  def chercherJoueur(player: ActorRef): Option[(ActorRef, (Int, Int))] = {
    return Option(players.find(_._1 == player).get)
  }

  def positionJoueur(player: ActorRef): Option[(Int, Int)] = {
    chercherJoueur(player) match {
      case Some(j) => return Option(j._2)
      case None => return None
    }
  }

  // TODO: Receive of winning the game
  // TODO: Génération de la map par rapport à une requete reçue

  def receive: Receive = {
    case GenerateMap => generateMap()
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
          var posx = j._2._1 + inci
          var posy = j._2._2 + incj
          if (posx > taille/2 || posx < 0) posx = j._2._1
          if (posy > taille/2 || posy < 0) posy = j._2._2
          players = players + (j._1 -> (posx, posy))
          // En présence d'un monstre
          if (map(posx)(posy)("monstre") == 1) {
            val resFuture = (player ? Player.GetLife).mapTo[Int]
            var life = Await.result(resFuture, 5 seconds)
            j._1 ! Player.Degats(dgts)
            val resFutureLife = (player ? Player.GetLife).mapTo[Int]
            life = Await.result(resFutureLife, 5 seconds)
            log.info(player.path.name + ": -" + dgts + ", " + life)
          }
        }
     }
    case msg @ _ => log.info(s"Message : $msg")
  }

}

object Player {
  case class Degats(valeur: Int)
  case object GetLife
  def apply(): Props = Props(new Player())
}
class Player extends Actor with ActorLogging {

  // TODO: Action to fight a monster with player
  // TODO: Joueur mort peut demander à etre retiré des joueurs de la partie

  import Player._

  var life = 100
  val currSender: ActorRef = sender()

  def getLife(): Unit = {
    sender() ! life
  }

  def receive: Receive = {
    case Degats(valeur) =>
      life -= valeur
      if (life < 0) life = 0
    case GetLife =>
      sender ! getLife()
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
  move(maher, "nord")
  move(maher, "nord")
  move(maher, "nord")
  move(maher, "nord")

  def move(j: ActorRef, d: String): Unit = {
    carte ! MoveJoueur(j, d)
    Thread.sleep(1000)
    carte ! PositionJoueur(j)
    Thread.sleep(1000)
  }

  System.exit(0)

}