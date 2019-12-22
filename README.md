# iwanttogoout

Serveur de jeu simpliste. Sur une map T x T, les joueurs tenteront de survivre en trouvant la sortie du labyrinthe.

--------------------------------------------------------------------------------

## Le jeu

### Acteurs

#### Game

Celui qui gère la map, les joueurs dans celle-ci et les attaques de monstres. Un game connait la taille de sa map, sa map et les joueurs présent en jeu.

- `generateMap` : Génération de la map aléatoire et création du chemin de sortie.
- `NewPlayer(player)` : Ajout de joueurs dans la partie.
- `PositionJoueur(player)` : Récupération de la position d'un joueur (ex: (1; 0)).
- `MoveJoueur(player, direction)` : Déplacement d'un joueur par rapport à une direction donnée (nord, sud, est, ouest).
- `chercherJoueur` : Permet de chercher si in joueur se trouve bien dans la partie.
- `AttackPlayer(player)` : Fais perdre de la vie aléatoirement (1 à 100) à un joueur.

#### Player

Un joueur possède de la vie.

- `Degats(valeur)` : Fais subir à un joueur des dégats.
- `Stats` : Renvoi au sender les informations du joueur.
