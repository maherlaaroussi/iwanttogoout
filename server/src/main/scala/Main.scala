import akka.stream.ActorMaterializer
import com.maherlaaroussi.iwanttogoout.Game
import akka.actor.{ActorLogging, ActorSystem}
import akka.http.scaladsl.Http
import io.swagger.server.model._
import spray.json.DefaultJsonProtocol
import akka.stream.ActorMaterializer
import akka.Done
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import io.swagger.server.api.{DefaultApi, DefaultApiMarshaller, DefaultApiService}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

import scala.concurrent.Future
import scala.io.StdIn

import com.maherlaaroussi.iwanttogoout.TheGame
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._


object Main extends App {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  val thegame = new TheGame(system)

  object DefaultMarshaller extends DefaultApiMarshaller {
    import DefaultJsonProtocol._

    def toEntityMarshallerJoueur: ToEntityMarshaller[Joueur] = jsonFormat3(Joueur)
    def toEntityMarshallerJoueurs: ToEntityMarshaller[Joueurs] = jsonFormat1(Joueurs)
    def toEntityMarshallerCarte: ToEntityMarshaller[Carte] = jsonFormat3(Carte)
  }

  object DefaultService extends DefaultApiService {

    import com.maherlaaroussi.iwanttogoout.Game._

    import com.maherlaaroussi.iwanttogoout.Player._
    implicit val timeout = new Timeout(2 seconds)

    def joueursNamePost(name: String)
                 (implicit toEntityMarshallerJoueur: ToEntityMarshaller[Joueur]): Route = {
      val reponse = (thegame.game ? NewPlayer(name)).mapTo[Joueur]
      requestcontext => {
        (reponse).flatMap {
          (joueur: Joueur) =>
            if (!joueur.name.equals("")) joueursNamePost200(joueur)(toEntityMarshallerJoueur)(requestcontext)
            else joueursNamePost400(requestcontext)
        }
      }
    }

    def joueursNameDelete(name: String): Route = {
      val reponse = (thegame.game ? DeletePlayer(name)).mapTo[Boolean]
      requestcontext => {
        (reponse).flatMap {
          (succes: Boolean) =>
            if (succes) joueursNameDelete200(requestcontext)
            else joueursNameDelete404(requestcontext)
        }
      }
    }

    def joueursNamePut(name: String, newName: String): Route = {
      val reponse = (thegame.game ? UpdatePlayer(name, newName)).mapTo[Int]
      requestcontext => {
        (reponse).flatMap {
          (rsp: Int) =>
            if (rsp == 0) joueursNamePut404(requestcontext)
            else if (rsp == -1) joueursNamePut406(requestcontext)
            else joueursNamePut200(requestcontext)
        }
      }
    }

    def joueursNameGet(name: String)(implicit toEntityMarshallerJoueur: ToEntityMarshaller[Joueur]): Route = {
      requestcontext => {
        joueursNameGet404(requestcontext)
      }
    }

    def joueursGet()(implicit toEntityMarshallerJoueurs: ToEntityMarshaller[Joueurs]): Route = {
      requestcontext => {
        joueursGet404(requestcontext)
      }
    }

    def mapGet()(implicit toEntityMarshallerCarte: ToEntityMarshaller[Carte]): Route = {
      requestcontext => {
        mapGet404(requestcontext)
      }
    }

    def joueursNameMovePost(name: String, direction: String)(implicit toEntityMarshallerJoueur: ToEntityMarshaller[Joueur]): Route = {
      requestcontext => {
        joueursNameMovePost404(requestcontext)
      }
    }

  }


  val api = new DefaultApi(DefaultService, DefaultMarshaller)

  val host = "localhost"
  val port = 8080

  val bindingFuture = Http().bindAndHandle(pathPrefix("api"){api.route}, host, port)
  println(s"Server online at http://${host}:${port}/\nPress RETURN to stop...")

  bindingFuture.failed.foreach { ex =>
    println(s"${ex} Failed to bind to ${host}:${port}!")
  }

  StdIn.readLine()
  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())

}
