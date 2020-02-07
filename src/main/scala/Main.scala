import akka.stream.ActorMaterializer
import com.maherlaaroussi.iwanttogoout.Game
import akka.actor.{ActorLogging, ActorSystem}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.Done
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import io.swagger.server.api.DefaultApiMarshaller
import io.swagger.server.model.Joueur
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._


object Main extends App {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  object DefaultMarshaller extends DefaultApiMarshaller {
    def toEntityMarshallerJoueur: ToEntityMarshaller[Joueur] = jsonFormat3(Joueur)
  }

  System.exit(0)

}
