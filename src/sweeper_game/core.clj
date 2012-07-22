(ns sweeper-game.core)

(defn -main
  "I don't do a whole lot."
  [& args]
  (println "Hello, World!"))

(defn open [pos board]
  (if (= ((board (first pos)) (second pos)) :bomb) :bomb
    1)
  )
