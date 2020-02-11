package com.maherlaaroussi.iwanttogoout
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, DeadLetter, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random
import io.swagger.server.model._

object Game {
  case class NewPlayer(name: String)
  case class DeletePlayer(name: String)
  case class UpdatePlayer(name: String, newName: String)
  case class PositionJoueur(player: ActorRef)
  case object GenerateMap
  case class MoveJoueur(player: ActorRef, direction: String)
  def apply(system: ActorSystem): Props = Props(new Game(system))
}
class Game(system: ActorSystem) extends Actor with ActorLogging {

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

  def findPlayerWithName(name: String): Option[(ActorRef, (Int, Int))] = {
    return players.find(_._1.path.name == name)
  }

  def sayonaraPlayer(player: ActorRef): Unit = {
    val r = new Random
    var messages: List[String] = List(
      "que son âme repose en paix.",
      "c'est pas comme si on était triste :/",
      "c'était à prévoir.",
      "un joueur en moins ..."
    )
    dead_players = dead_players + players.find(_._1 == player).get._1
    players = players - player
    system.stop(player)
    log.info("Le joueur " + player.path.name + " est parti, " + messages(r.nextInt(messages.length)))
  }

  def positionJoueur(player: ActorRef): Option[(Int, Int)] = {
    findPlayer(player) match {
      case Some(j) => return Option(j._2)
      case None => return None
    }
  }

  def receive: Receive = {
    case GenerateMap => generateMap()
    case NewPlayer(name) => findPlayerWithName(name) match {
        case Some(j) =>
          sender ! Joueur("", 0, "")
        case None =>
          var player = system.actorOf(Player(), name)
          players += (player -> (taille/2, taille/2))
          log.info("Le joueur " + player.path.name + " vient de rejoindre la partie.")
          sender ! Joueur(name, 100, "(" + taille/2 + ", " + taille/2 + ")")
      }
    case DeletePlayer(name) => findPlayerWithName(name) match {
        case Some(j) =>
          sayonaraPlayer(j._1)
          sender ! true
        case None => sender ! false
      }
    case UpdatePlayer(name, newName) => findPlayerWithName(name) match {
      case Some(j) => findPlayerWithName(newName) match {
        case Some(p) => sender ! -1
        case None =>
          // Si le joueur existe et qu'aucun autre joueur ne porte som nouveau nom
          sender ! 1
      }
      case None => sender ! 0
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

class TheGame(system: ActorSystem) {
  // TODO: Message pour comfirmer la création d'un joueur coté serveur
  val game = system.actorOf(Game(system), "game")
}

object ApiMessages {
  case class Joueur(name: String, life: Int, position: String)
}

