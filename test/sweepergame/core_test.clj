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

(def big-board [[0 0 0 0 0 0 0 0]
                [0 0 0 0 0 0 0 0]
                [:bomb 0 0 :bomb 0 0 0 :bomb]
                [0 0 0 0 0 0 0 0]
                [0 0 0 0 :bomb 0 0 0]
                [:bomb 0 0 :bomb 0 0 0 :bomb]
                [:bomb 0 0 0 :bomb :bomb 0 0]
                [0 0 0 0 0 0 0 0]
                  ])

(deftest test-feedback
  (testing "That it gives the right feedback"
    (is (= {:result :bomb :pos [0 1] :board [[0 :bomb] [0 0]]} (open [0 1] [[0 :bomb] [0 0]] false)) "Opening a bomb gives bomb")
    (is (= {:result :open :pos [0 1] :board [[0 :open] [0 0]]} (open [0 1] [[0 :open] [0 0]] false)) "Opening already opened")
    (is (= {:result 1 :pos [1 1] :board [[0 0 0] [:bomb :open 0] [0 0 0]]} (open [1 1] [[0 0 0] [:bomb 0 0] [0 0 0]] false)) "Opening a field gives updated value")
    (is (= 1 ((open [0 0] [[0 0 0] [:bomb 0 0] [0 0 0]] false) :result)) "Opening upper left")
    (is (= 0 ((open [0 0] big-board false) :result)) "Big board")
    (is (not (nil? ((open [0 0] (random-board 8 8 10) false) :result))) "Big board random")

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
  (is (= {:result [1 2] :count 1 :board
    [[:open :open :open] [:bomb :bomb :hint] [:open :open :open]]}  (hint [[:open :open :open] [:bomb :bomb 0] [:open :open :open]])))
  ))

(deftest test-finished
  (testing "That indication of finished is given"
    (is (not (finished? (random-board 4 8 1))) "A startboard is not finished")
    (is (finished? [[:bomb :open :open] [:hint :bomb :open] [:open :open :bomb]]) "All opened is finished")
    )
  )

(deftest test-coordinates
  (testing "That it gives all coordinates"
    (is (= [[[0 0] [0 1] [0 2]] [[1 0] [1 1] [1 2]]]
           (board-coordinates 2 3)))))

(deftest test-debug-board
  (testing "That it gives the right info to player"
    (is (= [{:row ["u" 1 "u"]} {:row [0 "u" "u"]}] (debug-board [[0 :open :bomb] [:hint 0 0]])
            ))))