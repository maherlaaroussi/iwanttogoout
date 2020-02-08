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
      post {
        
          
            
              
                
                  defaultService.joueurCreateNamePost(name = name)
               
             
           
         
       
      }
    } ~
    path("joueur" / "delete" / Segment) { (name) => 
      delete {
        
          
            
              
                
                  defaultService.joueurDeleteNameDelete(name = name)
               
             
           
         
       
      }
    } ~
    path("joueur" / "update" / Segment) { (name, `new`) => 
      put {
        
          
            
              
                
                  defaultService.joueurUpdateNamePut(name = name, `new` = `new`)
               
             
           
         
       
      }
    }
}

trait DefaultApiService {

  def joueurCreateNamePost200(responseJoueur: Joueur)(implicit toEntityMarshallerJoueur: ToEntityMarshaller[Joueur]): Route =
    complete((200, responseJoueur))
  /**
   * Code: 200, Message: La position du joueur dans la map, sa vie et son nom., DataType: Joueur
   */
  def joueurCreateNamePost(name: String)
      (implicit toEntityMarshallerJoueur: ToEntityMarshaller[Joueur]): Route

  def joueurDeleteNameDelete200(responseBoolean: Boolean): Route =
    complete((200, responseBoolean))
  /**
   * Code: 200, Message: Boooléan de la réponse du serveur., DataType: Boolean
   */
  def joueurDeleteNameDelete(name: String): Route

  def joueurUpdateNamePut200(responseJoueur: Joueur)(implicit toEntityMarshallerJoueur: ToEntityMarshaller[Joueur]): Route =
    complete((200, responseJoueur))
  /**
   * Code: 200, Message: Renvoi les informations actuelles du joueur dans le jeu., DataType: Joueur
   */
  def joueurUpdateNamePut(name: String, `new`: String)
      (implicit toEntityMarshallerJoueur: ToEntityMarshaller[Joueur]): Route

}

trait DefaultApiMarshaller {

  implicit def toEntityMarshallerJoueur: ToEntityMarshaller[Joueur]

  implicit def toEntityMarshallerJoueur: ToEntityMarshaller[Joueur]

}

