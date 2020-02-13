package io.swagger.server.api

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.FromRequestUnmarshaller
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import io.swagger.server.AkkaHttpHelper._
import io.swagger.server.model.Carte
import io.swagger.server.model.Joueur
import io.swagger.server.model.Joueurs

class DefaultApi(
    defaultService: DefaultApiService,
    defaultMarshaller: DefaultApiMarshaller
) {
  import defaultMarshaller._

  lazy val route: Route =
    path("joueurs") { 
      get {
        
          
            
              
                
                  defaultService.joueursGet()
               
             
           
         
       
      }
    } ~
    path("joueurs" / Segment) { (name) => 
      delete {
        
          
            
              
                
                  defaultService.joueursNameDelete(name = name)
               
             
           
         
       
      }
    } ~
    path("joueurs" / Segment) { (name) => 
      get {
        
          
            
              
                
                  defaultService.joueursNameGet(name = name)
               
             
           
         
       
      }
    } ~
    path("joueurs" / Segment / "move") { (name) => 
      post {
        parameters("direction".as[String]) { (direction) =>
          
            
              
                
                  defaultService.joueursNameMovePost(name = name, direction = direction)
               
             
           
         
        }
      }
    } ~
    path("joueurs" / Segment) { (name) => 
      post {
        
          
            
              
                
                  defaultService.joueursNamePost(name = name)
               
             
           
         
       
      }
    } ~
    path("joueurs" / Segment) { (name) => 
      put {
        parameters("name".as[String]) { (name2) =>
          
            
              
                
                  defaultService.joueursNamePut(name = name, name2 = name2)
               
             
           
         
        }
      }
    } ~
    path("map") { 
      get {
        
          
            
              
                
                  defaultService.mapGet()
               
             
           
         
       
      }
    }
}

trait DefaultApiService {

  def joueursGet200(responseJoueurs: Joueurs)(implicit toEntityMarshallerJoueurs: ToEntityMarshaller[Joueurs]): Route =
    complete((200, responseJoueurs))
  def joueursGet404: Route =
    complete((404, "Aucun joueur en jeu."))
  /**
   * Code: 200, Message: La liste des joueurs., DataType: Joueurs
   * Code: 404, Message: Aucun joueur en jeu.
   */
  def joueursGet()
      (implicit toEntityMarshallerJoueurs: ToEntityMarshaller[Joueurs]): Route

  def joueursNameDelete200: Route =
    complete((200, "Le joueur a été supprimé."))
  def joueursNameDelete404: Route =
    complete((404, "Le joueur n&#39;existe pas."))
  /**
   * Code: 200, Message: Le joueur a été supprimé.
   * Code: 404, Message: Le joueur n&#39;existe pas.
   */
  def joueursNameDelete(name: String): Route

  def joueursNameGet200(responseJoueur: Joueur)(implicit toEntityMarshallerJoueur: ToEntityMarshaller[Joueur]): Route =
    complete((200, responseJoueur))
  def joueursNameGet404: Route =
    complete((404, "Le joueur n&#39;existe pas."))
  /**
   * Code: 200, Message: Les informations du joueur., DataType: Joueur
   * Code: 404, Message: Le joueur n&#39;existe pas.
   */
  def joueursNameGet(name: String)
      (implicit toEntityMarshallerJoueur: ToEntityMarshaller[Joueur]): Route

  def joueursNameMovePost200(responseJoueur: Joueur)(implicit toEntityMarshallerJoueur: ToEntityMarshaller[Joueur]): Route =
    complete((200, responseJoueur))
  def joueursNameMovePost404: Route =
    complete((404, "Impossible d&#39;executer cette action."))
  /**
   * Code: 200, Message: Les informations du joueur., DataType: Joueur
   * Code: 404, Message: Impossible d&#39;executer cette action.
   */
  def joueursNameMovePost(name: String, direction: String)
      (implicit toEntityMarshallerJoueur: ToEntityMarshaller[Joueur]): Route

  def joueursNamePost201(responseJoueur: Joueur)(implicit toEntityMarshallerJoueur: ToEntityMarshaller[Joueur]): Route =
    complete((201, responseJoueur))
  def joueursNamePost400: Route =
    complete((400, "Impossible de créer le joueur."))
  /**
   * Code: 201, Message: La position du joueur dans la map, sa vie et son nom., DataType: Joueur
   * Code: 400, Message: Impossible de créer le joueur.
   */
  def joueursNamePost(name: String)
      (implicit toEntityMarshallerJoueur: ToEntityMarshaller[Joueur]): Route

  def joueursNamePut200: Route =
    complete((200, "Le nom du joueur a été modifié."))
  def joueursNamePut404: Route =
    complete((404, "Le joueur n&#39;existe pas."))
  def joueursNamePut406: Route =
    complete((406, "Le nouveau nom du joueur est déjà utilisé."))
  /**
   * Code: 200, Message: Le nom du joueur a été modifié.
   * Code: 404, Message: Le joueur n&#39;existe pas.
   * Code: 406, Message: Le nouveau nom du joueur est déjà utilisé.
   */
  def joueursNamePut(name: String, name2: String): Route

  def mapGet200(responseCarte: Carte)(implicit toEntityMarshallerCarte: ToEntityMarshaller[Carte]): Route =
    complete((200, responseCarte))
  def mapGet404: Route =
    complete((404, "La map est dans le néant."))
  /**
   * Code: 200, Message: La map actuelle du jeu., DataType: Carte
   * Code: 404, Message: La map est dans le néant.
   */
  def mapGet()
      (implicit toEntityMarshallerCarte: ToEntityMarshaller[Carte]): Route

}

trait DefaultApiMarshaller {

  implicit def toEntityMarshallerJoueurs: ToEntityMarshaller[Joueurs]

  implicit def toEntityMarshallerJoueur: ToEntityMarshaller[Joueur]

  implicit def toEntityMarshallerCarte: ToEntityMarshaller[Carte]

}

