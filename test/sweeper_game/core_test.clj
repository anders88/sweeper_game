(ns sweeper-game.core-test
  (:use clojure.test
        sweeper-game.core))

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
    (is (= :bomb (open [0 1] [[0 :bomb] [0 0]])) "Opening a bomb gives bomb")
    (is (= 1 (open [1 1] [[0 0 0] [:bomb 0 0] [0 0 0]])))
))