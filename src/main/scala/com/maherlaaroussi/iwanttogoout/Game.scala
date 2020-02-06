package com.maherlaaroussi.iwanttogoout
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, DeadLetter, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random

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
  val caseWin: (Int, Int) = (0, taille/2)
  var party = false
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
    // Il part du centre de la carte et finit tout en bas
    for (j <- 0 until taille/2) {
      map(taille/2)(j) = map(taille/2)(j) + ("sud" -> 1)
    }
    party = true
  }

  def destroyMap(): Unit = {
    party = false
    }

  def findPlayer(player: ActorRef): Option[(ActorRef, (Int, Int))] = {
    return players.find(_._1 == player)
  }

  def sayonaraPlayer(player: ActorRef): Unit = {
    val r = new Random
    var messages: List[String] = List(
      "il/elle n'ira sûrement pas au paradis.",
      "il/elle était nul/nulle, vraiment nul/nulle ...",
      "c'était à prévoir.",
      "arrête le jeu franchement ..."
    )
    dead_players = dead_players + players.find(_._1 == player).get._1
    players = players - player
    log.info(player.path.name + " est mort/morte, " + messages(r.nextInt(messages.length)))
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

      // S'il existe bien une map dans le jeu
      if (party) {

        players map { j =>
          if (j._1 == player) {

            var move = player.path.name + ": " + positionJoueur(player).getOrElse("None") + " -> "
            var inci = Map("est" -> -1, "ouest" -> 1).withDefaultValue(0)(direction)
            var incj = Map("nord" -> 1, "sud" -> -1).withDefaultValue(0)(direction)
            var dgts = 1 + r.nextInt(100)
            var f = Future { 0 }

            // Calcul de la nouvelle position
            var posx = j._2._1 + incj
            var posy = j._2._2 + inci
            val posMax = taille - 1
            if (posx > posMax || posx < 0) posx = j._2._1
            if (posy > posMax || posy < 0) posy = j._2._2
            players = players + (j._1 -> (posx, posy))
            move = move + positionJoueur(player).getOrElse("None")

            // En présence d'un monstre
            if (map(posx)(posy)("monstre") == 1) {
              f = (player ? Player.GetLife).mapTo[Int]
              var life = Await.result(f, 5 seconds)
              j._1 ! Player.Damage(dgts)
              f = (player ? Player.GetLife).mapTo[Int]
              life = Await.result(f, 5 seconds)
              log.info(move)
              log.info(player.path.name + ": -" + dgts + ", Life: " + life)
              if (life == 0) { sayonaraPlayer(player) }
            }

            // Le joueur a trouvé la sortie
            if (j._2 == caseWin) {
              log.info("Vous êtes sorti !!")
              destroyMap()
            }

          }
        }
      }
      // Dans le cas où il n'y a pas de map
      else {
        log.info("La map a été réduite à néant :o !")
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
  implicit val timeout: Timeout = new Timeout(10 seconds)

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
