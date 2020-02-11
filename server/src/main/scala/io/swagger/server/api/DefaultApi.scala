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
    path("joueur" / "update" / Segment / Segment) { (name, newName) => 
      put {
        
          
            
              
                
                  defaultService.joueurUpdateNameNewNamePut(name = name, newName = newName)
               
             
           
         
       
      }
    }
}

trait DefaultApiService {

  def joueurCreateNamePost200(responseJoueur: Joueur)(implicit toEntityMarshallerJoueur: ToEntityMarshaller[Joueur]): Route =
    complete((200, responseJoueur))
  def joueurCreateNamePost400: Route =
    complete((400, "Impossible de créer le joueur."))
  /**
   * Code: 200, Message: La position du joueur dans la map, sa vie et son nom., DataType: Joueur
   * Code: 400, Message: Impossible de créer le joueur.
   */
  def joueurCreateNamePost(name: String)
      (implicit toEntityMarshallerJoueur: ToEntityMarshaller[Joueur]): Route

  def joueurDeleteNameDelete200: Route =
    complete((200, "Le joueur a été supprimé."))
  def joueurDeleteNameDelete404: Route =
    complete((404, "Le joueur n&#39;existe pas."))
  /**
   * Code: 200, Message: Le joueur a été supprimé.
   * Code: 404, Message: Le joueur n&#39;existe pas.
   */
  def joueurDeleteNameDelete(name: String): Route

  def joueurUpdateNameNewNamePut200: Route =
    complete((200, "Le nom du joueur a été modifié."))
  def joueurUpdateNameNewNamePut404: Route =
    complete((404, "Le joueur n&#39;existe pas."))
  def joueurUpdateNameNewNamePut406: Route =
    complete((406, "Le nouveau nom du joueur est déjà utilisé."))
  /**
   * Code: 200, Message: Le nom du joueur a été modifié.
   * Code: 404, Message: Le joueur n&#39;existe pas.
   * Code: 406, Message: Le nouveau nom du joueur est déjà utilisé.
   */
  def joueurUpdateNameNewNamePut(name: String, newName: String): Route

}

trait DefaultApiMarshaller {

  implicit def toEntityMarshallerJoueur: ToEntityMarshaller[Joueur]

}

