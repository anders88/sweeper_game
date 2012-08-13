(ns sweepergame.core)

(defn calculate-board [board pos newval]
  (let [y (first pos) x (second pos)]
  (assoc board y (assoc (board y) x newval)))
  )

(defn contains-what? [what pos board]
  (let [y (first pos) x (second pos)]
  (and (>= x 0) (>= y 0) (< y (count board)) (< x (count (board 0)))
  (= what ((board y) x))))
  )

(defn bomb? [pos board]
  (contains-what? :bomb pos board)
  )

(defn neighbours [pos]
  (for [xd (range -1 2) yd (range -1 2)] [(+ yd (first pos)) (+ xd (second pos))])
  )

(defn open [pos board]  
  (cond (bomb? pos board) {:result :bomb :board board}    
        (contains-what? :open pos board) {:result :open :board board}
    :else
    {:result (count (filter #(bomb? % board) (neighbours pos))) 
    :board (calculate-board board pos :open)})
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
  (vec (map (fn[row] 
    (vec (map #(if (contains? bombs %) :bomb 0) row))) (board-coordinates y x)) 
  )))

(defn hint [board]
  (let [hint-pos 
  (first (pick-random (filter #(
    not (or (= :bomb ((board (first %)) (second %)))
        (= :open ((board (first %)) (second %))))
    )
  (for [indy (range 0 (count board)) indx (range 0 (count (board 0)))] [indy indx])
  ) 1))]
  {:result hint-pos :board (calculate-board board hint-pos :open)}
))