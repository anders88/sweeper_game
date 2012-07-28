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

(defn board-coordinates [y x]
  (partition-all x (for [indy (range 0 y) indx (range 0 x)] [indy indx]))
  )

(defn remove-item [listing number]
  (concat (subvec (vec listing) 0 number) 
        (if (= (inc number) (count listing)) [] (subvec (vec listing) (inc number))))
  )

(defn pick-random [listing picks]
  (if (<= picks 0) []
  (let [number (rand-int (count listing))]
  (cons ((vec listing) number) (pick-random 
        (remove-item listing number)  
        (dec picks)))
  )))

(defn random-board [y x num-bombs]
  (let [bombs 
    (set (pick-random (for [indx (range 0 x) indy (range 0 y)] [indy indx]) num-bombs))] 
  (map (fn[row] 
    (map #(if (contains? bombs %) :bomb 0) row)) (board-coordinates y x)) 
  ))

(defn hint [board opened]
  (first (pick-random (filter #(
    not (or (= :bomb ((board (first %)) (second %)))
        ((opened (first %)) (second %)))
    )
  (for [indy (range 0 (count board)) indx (range 0 (count (board 0)))] [indy indx])
  ) 1)))