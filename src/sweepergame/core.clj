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
  (let [y (first pos) x (second pos)]
  (and (>= x 0) (>= y 0) (< y (count board)) (< x (count (board 0)))
  (= :bomb ((board y) x))))
  )


(defn neighbours [pos]
  (for [xd (range -1 2) yd (range -1 2)] [(+ yd (first pos)) (+ xd (second pos))])
  )

(defn offboard? [pos board]
  (let [y (first pos) x (second pos)]
    (or (< x 0) (< y 0) (>= x (count (board 0))) (>= y (count board)))
  ))

(defn open [pos board allow-reopen]
  (cond (offboard? pos board) {:result :bomb :board board :pos pos :reopened false}
        (bomb? pos board) {:result :bomb :board board :pos pos :reopened false}
        (and allow-reopen (or (contains-what? :open pos board) (contains-what? :hint pos board))) 
            {:result (count (filter #(bomb? % board) (neighbours pos))) :board board :pos pos :reopened true}
                                                              
        (or (contains-what? :open pos board) (contains-what? :hint pos board)) {:result :open :board board :pos pos :reopened false}         
    :else
    {:result (count (filter #(bomb? % board) (neighbours pos)))
    :board (calculate-board board pos :open)
    :pos pos
    :reopened false
    })
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



(defn gen-new-board [board-rows board-cols board-bombs] (random-board board-rows board-cols board-bombs))

(defn count-neighbour-bombs [board pos]
  (count (filter #(bomb? % board) (neighbours pos)))
)

(defn hint [board]
  (let [hint-pos
  (first (pick-random (filter #(
    not (or (= :bomb ((board (first %)) (second %)))
        (= :open ((board (first %)) (second %)))
        (= :hint ((board (first %)) (second %))))
    )
  (for [indy (range 0 (count board)) indx (range 0 (count (board 0)))] [indy indx])
  ) 1))]
  {:result hint-pos
  :count (count-neighbour-bombs board hint-pos)
  :board (calculate-board board hint-pos :hint)}
))

(defn finished? [board]
  (not (contains? (set (reduce concat board)) 0))
  )

(defn finished-or-failed? [result]
  (or (finished? (result :board)) (= :open (result :result)) (= :bomb (result :result)))
  )


(defn replace-if-finished [result board-rows board-cols board-bombs]
  (if (finished-or-failed? result)
    (gen-new-board board-rows board-cols board-bombs)
    (result :board)
  )
  )

(defn number-of-hints [board]
  (count (filter #(= % :hint) (reduce concat board)))
  )

(defn number-of-opens [board]
  (count (filter #(= % :open) (reduce concat board)))
  )

(defn debug-board [board]
  (map (fn [row] {:row (map (fn [pos]
                        (let [content ((board (first pos)) (second pos))]
                          (cond
                            (= content :open) [(count-neighbour-bombs board pos) "open"]
                            (= content :hint) [(count-neighbour-bombs board pos) "hint"]
                            :else ["?" "unknown"]))) row)})
 (board-coordinates (count board) (count (board 0)))
))