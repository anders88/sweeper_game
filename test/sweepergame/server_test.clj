(ns sweepergame.server-test
  (:use clojure.test
        sweepergame.server))

(defn reset-status [f]
  (dosync (ref-set status {:numplayers 0 :players {}}))
  (f)
)

(use-fixtures :each reset-status)



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
    (is (= [] (sort-by-score [])) "Empty")
    (is (=
            {:name "Luke" :points {:total 300 :finishedBoards 2}}

          (first (sort-by-score
            [{:name "Luke" :points {:total 300 :finishedBoards 2}}
            {:name "Darth" :points {:total 200 :finishedBoards 3}}
           ]
            )))
    "Checking first")
  ))


(deftest test-json-generation-of-score
  (testing "That that one score is correct"
   (is (= "[{\"name\":\"Luke\",\"total\":300,\"finishedBoards\":2,\"maxOnBoard\":4,\"bombed\":0},{\"name\":\"Darth\",\"total\":200,\"finishedBoards\":3,\"maxOnBoard\":5,\"bombed\":0}]"
          (gen-json-score
                       [{:name "Luke" :points {:total 300 :finishedBoards 2 :maxOnBoard 4 :bombed 0}}
                        {:name "Darth" :points {:total 200 :finishedBoards 3 :maxOnBoard 5 :bombed 0}}
                        ])))
))

(deftest test-add-player
  (testing "That the player is added"
    (let [playno (register-new-player "Darth")]
    (is (= 1 (@status :numplayers)))
    (is (= "Darth" ((player-object playno) :name)))
    )))