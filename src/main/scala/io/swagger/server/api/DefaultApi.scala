package io.swagger.server.api

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.FromRequestUnmarshaller
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import io.swagger.server.AkkaHttpHelper._
import io.swagger.server.model.Joueur

class DefaultApi(
    defaultService: DefaultApiService,
    defaultMarshaller: DefaultApiMarshaller
) {
  import defaultMarshaller._

  lazy val route: Route =
    path("joueur" / "create" / Segment) { (name) => 
      get {
        
          
            
              
                
                  defaultService.joueurCreateNameGet(name = name)
               
             
           
         
       
      }
    }
}

trait DefaultApiService {

  def joueurCreateNameGet200(responseJoueur: Joueur)(implicit toEntityMarshallerJoueur: ToEntityMarshaller[Joueur]): Route =
    complete((200, responseJoueur))
  /**
   * Code: 200, Message: La position du joueur dans la map, sa vie et son nom., DataType: Joueur
   */
  def joueurCreateNameGet(name: String)
      (implicit toEntityMarshallerJoueur: ToEntityMarshaller[Joueur]): Route

}

trait DefaultApiMarshaller {

  implicit def toEntityMarshallerJoueur: ToEntityMarshaller[Joueur]

}

