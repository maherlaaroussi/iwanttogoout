import akka.stream.ActorMaterializer
import com.maherlaaroussi.iwanttogoout.Game
import akka.actor.{ActorLogging, ActorSystem}
import akka.http.scaladsl.Http
import io.swagger.server.model.Joueur
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
import akka.stream.ActorMaterializer
import akka.Done
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import io.swagger.server.api.{DefaultApi, DefaultApiMarshaller, DefaultApiService}
import io.swagger.server.model.Joueur
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
    def toEntityMarshallerJoueur: ToEntityMarshaller[Joueur] = jsonFormat3(Joueur)
  }

  object DefaultService extends DefaultApiService {

    import com.maherlaaroussi.iwanttogoout.Game._

    import com.maherlaaroussi.iwanttogoout.Player._
    implicit val timeout = new Timeout(2 seconds)

    def joueurCreateNamePost(name: String)
                 (implicit toEntityMarshallerJoueur: ToEntityMarshaller[Joueur]): Route = {
      val reponse = (thegame.game ? NewPlayer(name)).mapTo[Joueur]
      requestcontext => {
        (reponse).flatMap {
          (joueur: Joueur) =>
            if (!joueur.name.equals("")) joueurCreateNamePost200(joueur)(toEntityMarshallerJoueur)(requestcontext)
            else joueurCreateNamePost400(requestcontext)
        }
      }

    }

    def joueurDeleteNameDelete(name: String): Route = {
      val reponse = (thegame.game ? DeletePlayer(name)).mapTo[Boolean]
      requestcontext => {
        (reponse).flatMap {
          (succes: Boolean) =>
            if (succes) joueurDeleteNameDelete200(requestcontext)
            else joueurDeleteNameDelete404(requestcontext)
        }
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
