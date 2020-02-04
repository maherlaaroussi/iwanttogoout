# iwanttogoout

Serveur de jeu simpliste. Sur une map T x T, les joueurs tenteront de survivre en trouvant la sortie du labyrinthe.

--------------------------------------------------------------------------------

# TODO :
- [X] Génération de la map.
- [X] Attaque aléatoire des joueurs à chaque déplaçement.
- [X] Attente d'une réponse de l'acteur (Utilisation du Await).
- [X] Utilisation des Options, Some et None.
- [ ] Suppression d'un joueur de la map.
- [ ] Déplacement d'un joueur dans la map.
- [X] Dégats sur un joueur.
- [X] Récupération de la vie d'un joueur (avec sender).
- [ ] Notification pour les joueurs mort.
- [ ] Notification quand la partie est fini
- [ ] Notification pour le joueur qui a gagné la partie.
- [ ] Possibilité de relancer une partie.

--------------------------------------------------------------------------------

# Le jeu :

## Acteurs

### Game

Celui qui gère la map, les joueurs dans celle-ci et les attaques de monstres. Un game connait la taille de sa map, sa map et les joueurs présent en jeu.

###### Variables :
- `map` : La map du jeu en Z x Z.
- `taille` : Taille de la map *(6 par défault)*. 
- `r` : Variable servant à générer un chiffre aléatoire.
- `players` : La liste de type Map des joueurs présent dans la carte avec leur position.
- `dead_players` : Un Set des joueurs mort.
- `timeout` : Utilisé pour les unités *(seconds, minutes ...)*.
- `executionContext`

###### Fonctions, Classes et Objects :
- `generateMap` **/** `GenerateMap()` : Génération de la map aléatoire et création du chemin de sortie.
- `NewPlayer(player)` : Ajout de joueurs dans la partie.
- `PositionJoueur(player)` : Récupération de la position d'un joueur (ex: (1; 0)).
- `MoveJoueur(player, direction)` : Déplacement d'un joueur par rapport à une direction donnée (nord, sud, est, ouest).
- `findPlayer` : Permet de chercher si in joueur se trouve bien dans la partie.
- `AttackPlayer(player)` : Fais perdre de la vie aléatoirement (1 à 100) à un joueur.
- `sayonaraPlayer(player)` **/** `DeletePlayer(player)` : Cherche si le joueur indiqué se trouve bien dans la carte, le supprime le cas échéant et le place avec les morts.

### Player

Un joueur possède de la vie.

###### Variables :
- `life` : La vie du joueur.

###### Fonctions, Classes et Objects :
- `Damage(value)` : Fais subir à un joueur des dégats.
- `GetLife` **/** `getLife` : Renvois la vie actuelle du joueur.
