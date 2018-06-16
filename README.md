### Installation and run

You need to install [httpie](https://httpie.org/) previously to easily run
http calls.

1. Run the server. This script will run the server in the background
    (it takes some time):
    ```
    $ ./run-server.sh
    ```
    _Important note_: this script also initializes `example-bots` so that
    the bots become individual git repositories (otherwise the server is
    unable to checkout the bots with `git`).

2. Then create and run the match with several bots from `example-bots`.
    ```
    $ ./run-match.sh
    ```
    After the match ends (the bots stop moving) just press Ctrl+C.

### Available endpoints

- [H2 console](localhost:8888/h2) access the internal database,
    use `jdbc:h2:mem:testdb;DB_CLOSE_ON_EXIT=FALSE` as jdbc url.
- [Swagger](localhost:8888/swagger-ui.html) access the Swagger documentation.

### Example command pipeline for creating and play match
-  asciinema rec
- ./example-bots-run.sh
### Notes on classes and interfaces

**Interface Bot**
Interface of bot.

**Class Head**
Class wraps implemented Bot instance. It contains
info for mapping on the field

**Class Field**
Class contains cells of field

**Class Match**
Class contains state of game for this match, match
duration, id of match, list of bots

**Class GameState**
Class contains field of match and list of dead bots

**Class GameStateView**
Class represents datamart of field of match , id bot, it's head point on the field

**Enumeration Move:**
Variant of bot moving. _LEFT, STOP ...._

**Class BotService**
Class returns instances of implemented bots

**Class FieldService**
Class creates field, print ant etc.

**Class GamePlayService**
Class creates match, changes state of game for each bot

**Class Application**
Class responsible for management of gameplay


Agreement for representing of game state:
The upper case simbol on the field means head of bot.
Example1:<br> A, B, C<br>
The lower case simbol on the field means tail of bot.
Example2:<br> a, b, c<br>
The digit simbol on the field means owned area .
Example3:<br> 1, 2, 3<br>
The space simbol on the field means free area .


**Deploy**
To thumbtack-xonix-mind/ansible add "prod" folder and add to it inventory for prod environment.
Then:
```
cd thumbtack-xonix-mind/ansible
./deploy.sh
```
