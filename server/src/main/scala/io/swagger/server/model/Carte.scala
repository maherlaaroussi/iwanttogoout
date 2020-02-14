package io.swagger.server.model


/**
 * @param taille 
 * @param monstres 
 * @param joueurs 
 * @param joueurs_morts 
 */
case class Carte (
  taille: Int,
  monstres: Int,
  joueurs: Int,
  joueurs_morts: Int
)

