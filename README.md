# iwanttogoout

Serveur de jeu simpliste. Sur une map T x T, les joueurs tenteront de survivre en trouvant la sortie du labyrinthe.

--------------------------------------------------------------------------------

# Le jeu :

## Principe

Un joueur peut être créé puis pourra être déplacé dans une map Z x Z. À chaque déplacement, le joueur peut être attaqué par un monstre s'il y en a dans la position du joueur. Arrivé à la limite de la map, le joueur ne pourra pas se déplacer en dehors des limites, mais chaque déplacement inutile fera qu'un monstre pourra vous attaquer. Le gagnant sera celui qui trouvera la sortie en premier. À la fin de la partie, on pourra relancer une autre partie.

## Acteurs

### Game

Celui qui gère la map, les joueurs dans celle-ci et les attaques de monstres. Un game connait la taille de sa map, sa map et les joueurs présent en jeu.

#### Variables :

- `map` : La map du jeu en Z x Z.
- `taille` : Taille de la map _(6 par défault)_.
- `r` : Variable servant à générer un chiffre aléatoire.
- `players` : La liste de type Map des joueurs présent dans la carte avec leur position.
- `dead_players` : Un Set des joueurs mort.
- `timeout` : Utilisé pour les unités _(seconds, minutes ...)_.
- `executionContext`

#### Fonctions, Classes et Objects :

- `generateMap` **/** `GenerateMap()` : Génération de la map aléatoire et création du chemin de sortie.
- `NewPlayer(player)` : Ajout de joueurs dans la partie.
- `PositionJoueur(player)` : Récupération de la position d'un joueur (ex: (1; 0)).
- `MoveJoueur(player, direction)` : Déplacement d'un joueur par rapport à une direction donnée (nord, sud, est, ouest).
- `findPlayer` : Permet de chercher si in joueur se trouve bien dans la partie.
- `AttackPlayer(player)` : Fais perdre de la vie aléatoirement (1 à 100) à un joueur.
- `sayonaraPlayer(player)` **/** `DeletePlayer(player)` : Cherche si le joueur indiqué se trouve bien dans la carte, le supprime le cas échéant et le place avec les morts.

### Player

Un joueur possède de la vie.

#### Variables :

- `life` : La vie du joueur.

#### Fonctions, Classes et Objects :

- `Damage(value)` : Fais subir à un joueur des dégats.
- `GetLife` **/** `getLife` : Renvois la vie actuelle du joueur.

--------------------------------------------------------------------------------

# L'API v1 :

## Routes :

- **GET** /joueurs
- **GET** /joueurs/{name}
- **POST** /joueurs/{name}
- **PUT** /joueurs/{name}
- **DELETE** /joueurs/{name}
- **POST** /joueurs/{name}/move
- **GET** /map
