(ns sweepergame.core-test
  (:use clojure.test
        sweepergame.core))

(deftest test-find-bomb
  (testing "That it indicates bombs on and off board"
    (is (bomb? [1 1] [[0 0 0] [0 :bomb 0] [0 0 0]]) "A bomb is on the board")
    (is (not (bomb?[1 0] [[0 0 0] [0 :bomb 0] [0 0 0]])) "No bomb there")
    (is (not (bomb?[1 4] [[0 0 0] [0 :bomb 0] [0 0 0]])) "No bombs offboard")
    )
  )

(deftest test-neighbours
  (testing "Finding neighbours"
    (is (= [[0 0] [1 0] [2 0] [0 1] [1 1] [2 1] [0 2] [1 2] [2 2]] (neighbours [1 1])))
    )
  )


(deftest test-feedback
  (testing "That it gives the right feedback"
    (is (= {:result :bomb :board [[0 :bomb] [0 0]]} (open [0 1] [[0 :bomb] [0 0]])) "Opening a bomb gives bomb")
    (is (= {:result :open :board [[0 :open] [0 0]]} (open [0 1] [[0 :open] [0 0]])) "Opening already opened")
    (is (= {:result 1 :board [[0 0 0] [:bomb :open 0] [0 0 0]]} (open [1 1] [[0 0 0] [:bomb 0 0] [0 0 0]])) "Opening a field gives updated value")
))

(deftest test-calculate-board
  (testing "That the board is updated"
    (is (= [[0 :bomb :open] [0 0 0]] (calculate-board [[0 :bomb 0] [0 0 0]] [0 2] :open)))
    )
  )

(deftest test-random-board
  (testing "That random board is generated"
    (is (= 4 (count (random-board 4 8 3))))
    (is (= 8 (count (first (random-board 4 8 3)))))
    (is (= 32 (count (filter #(= :bomb %) (reduce concat (random-board 4 8 32))))))
    (is (= 1 (count (filter #(= :bomb %) (reduce concat (random-board 4 8 1))))))
  ))

(deftest test-random-picker
  (testing "Pick random number"
    (is (= 1 (count (pick-random [1 2 3] 1))))
    (is (= 2 (count (pick-random [1 2 3] 2))))
    (is (= #{1 2 3 4 5} (set (pick-random [1 2 3 4 5] 5))))
    ))

(deftest test-hint
  (testing "That hint gives an unexplored field"
  (is (= [1 2] (hint [[:open :open :open] [:bomb :bomb 0] [:open :open :open]])))
  ))