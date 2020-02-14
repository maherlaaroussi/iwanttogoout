package com.maherlaaroussi.iwanttogoout
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, DeadLetter, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.maherlaaroussi.iwanttogoout.Game.GenerateMap
import com.maherlaaroussi.iwanttogoout.Player.Damage

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
  case object GetMap
  case object GetPlayers
  case class GetPlayer(name: String)
  case class MovePlayer(name: String, direction: String)
  def apply(system: ActorSystem): Props = Props(new Game(system))
}
class Game(system: ActorSystem) extends Actor with ActorLogging {

  import Game._
  import scala.collection.mutable.ListBuffer

  val r = scala.util.Random
  val taille = 6
  var caseWin: (Int, Int) = (0, taille/2)
  val posBegining = (taille/2, taille/2)
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

    // On défini de quelle coté sera la sortie (x ou y)
    var whichSide = r.nextInt(2)
    // Coté x
    if (whichSide == 0) {
      // Pour savoir si ce sera à l'est ou l'ouest
      whichSide = r.nextInt(2)
      // SORTIE: Ouest
      if (whichSide == 0) {
        caseWin = (0, taille/2)
        for (j <- 0 until taille/2) {
          map(j)(taille/2) = map(j)(taille/2) + ("sud" -> 1)
        }
      }
      // SORTIE: Est
      else {
        caseWin = (taille-1, taille/2)
        for (j <- taille/2 until taille-1) {
          map(taille-1)(j) = map(taille-1)(j) + ("nord" -> 1)
        }
      }
    }
    else {
      // Pour savoir si ce sera au sud ou au nord
      whichSide = r.nextInt(2)
      // SORTIE: Nord
      if (whichSide == 0) {
        caseWin = (taille/2, 0)
        for (j <- 0 until taille/2) {
          map(j)(taille/2) = map(j)(taille/2) + ("nord" -> 1)
        }
      }
      // SORTIE: Sud
      else {
        caseWin = (taille/2, taille-1)
        for (j <- 0 until taille/2) {
          map(j)(taille/2) = map(j)(taille/2) + ("nord" -> 1)
        }
      }
    }

    party = true
  }

  def destroyMap(): Unit = {
    party = false
  }

  def resetPlayers(): Unit = {
    dead_players foreach {
      j =>
        players = players + (j -> posBegining)
        dead_players = dead_players - j
    }
    players foreach {
      j =>
        j._1 ! Player.Reset
        players = players + (j._1 -> posBegining)
    }
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
      "que son âme repose en paix auprès du démon.",
      "c'est pas comme si on était triste :/",
      "c'était à prévoir.",
      "un joueur en moins ...",
      "il occupait trop de place dans le jeu pour rien :)"
    )
    dead_players = dead_players + players.find(_._1 == player).get._1
    players = players - player
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
    case GetPlayer(name) => findPlayerWithName(name) match {
      case Some(j) =>
        var f = (j._1 ? Player.GetInfos).mapTo[collection.mutable.Map[String, Any]]
        var infos = Await.result(f, 5 seconds)
        infos("position") = j._2.toString()
        sender ! Joueur(infos("name").toString, infos("life").asInstanceOf[Int], infos("position").toString)
      case None => sender ! Joueur("", 0, "(0,0)")
    }
    case GetMap =>
      if (party) {
        var monstres = 0
        map foreach (
          i => i foreach (
            j => j foreach (
              // Impossible de cast AnyVal to Boolean pour le pos._2, vraiment tout testé
              pos => if (pos._1.equals("monstre") && pos._2.toString().equals("1")) monstres += 1
            )
          )
        )
        sender ! Carte(taille, monstres, players.size, dead_players.size)
      }
      else {
        sender ! Carte(0, 0, 0, 0)
      }
    case GetPlayers =>
      var joueurs = new ListBuffer[String]()
      players foreach (
        j => joueurs += j._1.path.name
      )
      sender ! Joueurs(joueurs.toList)
    case NewPlayer(name) => findPlayerWithName(name) match {
        case Some(j) =>
          sender ! Joueur("", 0, "")
        case None =>
          var player = system.actorOf(Player(), name)
          players += (player -> posBegining)
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
    case MovePlayer(name, direction) =>

      var found = false

      // S'il existe bien une map dans le jeu
      if (party) {

        players foreach {
          j =>
            if (j._1.path.name == name) {
              found = true
              var player = j._1
              var move = player.path.name + ": " + positionJoueur(player).getOrElse("None") + " -> "
              var inci = Map("est" -> -1, "ouest" -> 1).withDefaultValue(0)(direction)
              var incj = Map("nord" -> 1, "sud" -> -1).withDefaultValue(0)(direction)
              var dgts = 1 + r.nextInt(100)
              var f = Future { 0 }
              f = (player ? Player.GetLife).mapTo[Int]
              var life = Await.result(f, 5 seconds)

              // Calcul de la nouvelle position
              var posx = j._2._1 + incj
              var posy = j._2._2 + inci
              val posMax = taille - 1
              if (posx > posMax || posx < 0) posx = j._2._1
              if (posy > posMax || posy < 0) posy = j._2._2
              players = players + (j._1 -> (posx, posy))
              move = move + positionJoueur(player).getOrElse("None")
              log.info(move)

              // En présence d'un monstre
              if (map(posx)(posy)("monstre") == 1) {
                player ! Damage(dgts)
                f = (player ? Player.GetLife).mapTo[Int]
                life = Await.result(f, 5 seconds)
                log.info(player.path.name + ": -" + dgts + ", Life: " + life)
                if (life == 0) {
                  sayonaraPlayer(player)
                  life = 666
                }
              }

              var position = positionJoueur(player).getOrElse("Enfer")

              // Le joueur a trouvé la sortie
              if (position == caseWin) {
                log.info(player.path.name + " a trouvé la sortie !!")
                position = "Freedom"

                // Nouvelle partie
                destroyMap()
                generateMap()
                resetPlayers()
              }

              sender ! Joueur(player.path.name, life, position.toString)

            }
        }

        // Si aucun joueur ne porte pas ce nom
        if (!found) sender ! Joueur("", 0, "")

      }
      // Dans le cas où il n'y a pas de map
      else {
        log.info("La map a été réduite à néant :o !")
        sender ! Joueur("", 0, "")
      }
    case msg @ _ => log.info(s"Message : $msg")
  }

}

object Player {
  case class Damage(value: Int)
  case object GetLife
  case object Reset
  case object GetInfos
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
    case GetInfos =>
      var infos = collection.mutable.Map(
        "name" -> self.path.name,
        "life" -> life,
        "position" -> "(0,0)"
      )
      sender ! infos
    case Reset =>
      life = 100
    case msg @ _ => log.info(s"Message : $msg")
  }

}

class TheGame(system: ActorSystem) {
  // TODO: Message pour comfirmer la création d'un joueur coté serveur
  val game = system.actorOf(Game(system), "game")
  game ! Game.GenerateMap
}

object ApiMessages {
  case class Joueur(name: String, life: Int, position: String)
}

