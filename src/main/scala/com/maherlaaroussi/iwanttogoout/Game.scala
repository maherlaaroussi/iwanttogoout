package com.maherlaaroussi.iwanttogoout
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.ExecutionContext

object Carte {
  case class NewPlayer(player: ActorRef)
  case class AttackPlayer(player: ActorRef)
  case class InfomationJoueur(player: ActorRef)
  def apply(): Props = Props(new Carte())
}
class Carte extends Actor with ActorLogging {

  import Carte._

  val r = scala.util.Random
  val taille = 5
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
    )
  }

  def chercherJoueur(player: ActorRef): Option[ActorRef] = {
    return Option(players.find(_._1 == player).get._1)
  }

  // TODO: Create the unique path to the exit
  // TODO: Create the class Monster
  // TODO: Receive of moving a player
  // TODO: Receive of winning the game

  // TODO: Mettre en place une liste de joueurs avec leur position
  def receive: Receive = {
    case NewPlayer(player) => players += (player -> (taille/2, taille/2))
    case AttackPlayer(player) => chercherJoueur(player) match {
      case Some(j) => j ! Player.Degats(1 + r.nextInt(100))
      case None => log.info("Ce joueur n'est pas dans la carte !")
    }
    case InfomationJoueur(player) => chercherJoueur(player) match {
      case Some(j) =>
        val infos = (j ? Player.Stats)
        infos.map(i => log.info("Result: " + i))
      case None => log.info("Ce joueur n'est pas dans la carte !")
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

  val systeme = ActorSystem("simplesys")
  val carte = systeme.actorOf(Carte(), "carte")
  val player = systeme.actorOf(Player(), "Maher")
  carte ! Carte.NewPlayer(player)
  carte ! Carte.AttackPlayer(player)
  carte ! Carte.InfomationJoueur(player)

}