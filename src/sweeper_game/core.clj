(ns sweeper-game.core)

(defn -main
  "I don't do a whole lot."
  [& args]
  (println "Hello, World!"))

(defn bomb? [pos board]
  (let [y (first pos) x (second pos)]
  (and (>= x 0) (>= y 0) (< y (count board)) (< x (count (board 0)))
  (= :bomb ((board y) x))))
  )

(defn neighbours [pos]
  (for [xd (range -1 2) yd (range -1 2)] [(+ yd (first pos)) (+ xd (second pos))])
  )

(defn open [pos board]
  (if (bomb? pos board) :bomb    
    (count (filter #(bomb? % board) (neighbours pos)))
    )
  )

(defn random-board [y x bombs]
  (repeat y (repeat x :bomb))
  )
