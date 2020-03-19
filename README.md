# iwanttogoout

(S'il y a une v2, elle s'appelera : **ireallywanttogoout**)

Serveur de jeu simpliste. Sur une map T x T, les joueurs tenteront de survivre en trouvant la sortie du labyrinthe.

--------------------------------------------------------------------------------

# Le jeu :

## Principe

Un joueur peut être créé puis pourra être déplacé dans une map Z x Z. À chaque déplacement, le joueur peut être attaqué par un monstre s'il y en a dans la position du joueur. Arrivé à la limite de la map, le joueur ne pourra pas se déplacer en dehors des limites, mais chaque déplacement inutile fera qu'un monstre pourra vous attaquer. Le gagnant sera celui qui trouvera la sortie en premier. À la fin de la partie, tous les joueurs vivants sont remis à la case de départ et la map est généré une nouvelle fois pour une nouvelle partie.

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
- `casWin` : La case qui permet à un joueur de sortir du labyrinthe.
- `posBegining` : Position de départ des joueurs.
- `party` : Indique si une partie est en cours.

#### Fonctions, Classes et Objects :

- `generateMap` **/** `GenerateMap()` : Génération de la map aléatoire et création du chemin de sortie.
- `destroyMap` : Mets simplement à jour la variable party pour indiquer que la partie est fini.
- `resetPlayers` : Place tous les joueurs morts avec les vivants puis place tous les joueurs à la position de départ pour la nouvelle partie.
- `findPlayer` : Permet de chercher si un joueur se trouve bien dans la partie.
- `findPlayerWithName` : Permet de chercher si un joueur, avec son nom, se trouve bien dans la partie.
- `sayonaraPlayer(player)` **/** `DeletePlayer(player)` : Cherche si le joueur indiqué se trouve bien dans la carte, le supprime le cas échéant et le place avec les morts.
- `positionJoueur` **/** `PositionJoueur(player)` : Permet d'obtenir la position actuelle d'un joueur.
- `GetPlayer` : Renvoie les informations d'un joueur.
- `GetMap` : Obtient les informations basiques de la map.
- `GetPlayers` : Récupère la liste de tous les joueurs présents dans la map.
- `NewPlayer(name)` : Ajoute un nouveau joueur à la partie.
- `UpdatePlayer(name, newName)` : Met à jour le nom d'un joueur **(NOT OKAY**
- `MovePlayer(name, direction)` : Déplace un joueur en fonction de la direction donnée.

### Player

Un joueur possède de la vie, c'en est déjà beacoup pour un simple joueur..

#### Variables :

- `life` : La vie du joueur.

#### Fonctions, Classes et Objects :

- `Damage(value)` : Fais subir à un joueur des dégats.
- `GetLife` **/** `getLife()` : Renvois la vie actuelle du joueur.
- `Reset` : Remet la vie à son état initial.

--------------------------------------------------------------------------------

# L'API v1 :

## Routes :

- **GET** /joueurs

  ```bash
  curl -X GET "http://localhost:8080/api/joueurs" -H "accept: */*"
  ```

- **GET** /joueurs/{name}

  ```bash
  curl -X GET "http://localhost:8080/api/joueurs/Maher" -H "accept: */*"
  ```

- **POST** /joueurs/{name}

  ```bash
  curl -X POST "http://localhost:8080/api/joueurs/Maher" -H "accept: */*"
  ```

- **PUT** /joueurs/{name}

  ```bash
  curl -X PUT "http://localhost:8080/api/joueurs/Maher?name=John" -H "accept: */*"
  ```

- **DELETE** /joueurs/{name}

  ```bash
  curl -X DELETE "http://localhost:8080/api/joueurs/Maher" -H "accept: */*"
  ```

- **POST** /joueurs/{name}/move

  ```bash
  curl -X POST "http://localhost:8080/api/joueurs/Maher/move?direction=nord" -H "accept: */*"
  ```

- **GET** /map

  ```bash
  curl -X GET "http://localhost:8080/api/map" -H "accept: */*"
  ```

--------------------------------------------------------------------------------

# En pratique **(Lisez tout !!)** :

- Lancement du serveur

  ```bash
  cd server && sbt run
  ```

- Création d'un joueur

  ```bash
  curl -X POST "http://localhost:8080/api/joueurs/John" -H "accept: */*"
  ```

- Déplacement dans la map

  ```bash
  curl -X POST "http://localhost:8080/api/joueurs/John/move?direction=nord" -H "accept: */*"
  curl -X POST "http://localhost:8080/api/joueurs/John/move?direction=sud" -H "accept: */*"
  curl -X POST "http://localhost:8080/api/joueurs/John/move?direction=ouest" -H "accept: */*"
  curl -X POST "http://localhost:8080/api/joueurs/John/move?direction=est" -H "accept: */*"
  ```

- Obtention d'information d'un joueur

  ```bash
  curl -X GET "http://localhost:8080/api/joueurs/John" -H "accept: */*"
  ```

- Obtention de la liste de tous les joueurs

  ```bash
  curl -X GET "http://localhost:8080/api/joueurs" -H "accept: */*"
  ```

- Obtention d'information sur la map

  ```bash
  curl -X GET "http://localhost:8080/api/map" -H "accept: */*"
  ```

- Suppression d'un joueur (Eh oui, cela supprime n'importe quel joueur)

  ```bash
  curl -X DELETE "http://localhost:8080/api/joueurs/John" -H "accept: */*"
  ```

- Après avoir erré dans le labyrinthe, au moment où vous trouverez la sortie, le serveur retournera cela

  ```json
  {"life":13,"name":"John","position":"Freedom"}
  ```

- Mais dans le cas où vous mourriez (Hahahaaha)

  ```json
  {"life":666,"name":"John","position":"Enfer"}
  ```

- Puis la map sera regénérée automatiquement et vous serez placé au centre de la map pour une nouvelle partie
