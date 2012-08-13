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
