(ns sweepergame.server-test
  (:use clojure.test
        sweepergame.server))

(deftest test-read-coordinates
  (testing "That coordinate are read from the parameters"
    (is (= [1 2] (read-coordinates {:y "1" :x "2"})) "Reading coordinate")
    (is (nil? (read-coordinates {})) "No coordinate")
    (is (nil? (read-coordinates {:y "1"})) "Only one coordinate")
    (is (nil? (read-coordinates {:y "1" :x "a"})) "Not numeric coordinate")
    )
  )


(deftest test-sorting-scores 
  (testing "That map is sorted by score"
    (is (= [{:name "Darth" :points {:total 200 :finishedBoards 3}}
            {:name "Luke" :points {:total 300 :finishedBoards 2}}
          ]
          (sort-by-score
            [{:name "Luke" :points {:total 300 :finishedBoards 2}}
            {:name "Darth" :points {:total 200 :finishedBoards 3}}
           ]
            )))
    )
    (is (= [] (sort-by-score [])))
  )