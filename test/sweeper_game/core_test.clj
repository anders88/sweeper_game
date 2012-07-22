(ns sweeper-game.core-test
  (:use clojure.test
        sweeper-game.core))

(deftest test-feedback
  (testing "That it gives the right feedback"
    (is (= :bomb (open [0 1] [[0 :bomb] [0 0]])) "Opening a bomb gives bomb")
    (is (= 1 (open [1 1] [[0 0 0] [:bomb 0 0] [0 0 0]])))
))