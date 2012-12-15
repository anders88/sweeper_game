# Getting started (Windows)

1. Install leiningen
  * Download leiningen-2.0.0-preview10-standalone.jar from https://github.com/technomancy/leiningen/downloads into e.g. c:\dev\bin
  * Create lein.bat in you path with the following line: @java -cp c:\dev\bin\leiningen-2.0.0-preview10-standalone.jar clojure.main -m leiningen.core.main %*
2. git clone http://github.com/anders88/sweeper_game.git
3. cd ...\sweeper_game
4. lein run
5. Go to http://localhost:1337

# Getting started (Linux)

1. Install leiningen
  * Go to https://github.com/technomancy/leiningen 
  * create/download the script (Follow the instruction in the Installation section) 
2. git clone http://github.com/anders88/sweeper_game.git
3. cd ...\sweeper_game
4. lein run
5. Go to http://localhost:1337

# Playing

1. Download a starting point from https://github.com/jhannes/minefield-solver
2. Go to http://localhost:1337
3. Enter a username and press Register
4. Change the userid in the application to what you were given by the application
5. Run the program
6. Change the program

# Administrative functions (optional)
Update password : lein trampoline run setPassword setupfile
