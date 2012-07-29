(ns sweepergame.server
  (:use noir.core)
  (:use noir.request)
  (:use sweepergame.core)
  (:require [noir.server :as server]))

(defpage "/" []
    "Welcome to Noiraazz")

(defpage "/open" []
  (str ((ring-request) :params))
  )

(defn board-str [board]
  (str "<html></body><table>"
  (reduce str 
    (map (fn [row] 
      (str "<tr>" (reduce str 
        (map #(str "<td>" (if (= :bomb %) "X" "0") "</td>") row)) 
       "</tr>")
   ) board)) 
   "</table></body></html>")
  )

(defpage "/new" []
  (board-str (random-board 8 8 10))
  )

(defn -main [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "1337"))
        ]
    (server/start port {:mode mode
                        :ns 'sweepergame})))