(ns sweepergame.server
  (:use [noir.core]
   [noir.request]
   [sweepergame.core]
   [hiccup.page-helpers :only [html5 include-js link-to unordered-list]]
   [hiccup.form-helpers])
  (:require [noir.server :as server]))

(def status (ref {:numplayers 0}))
(def debug true)
(def gen-new-board '(random-board 8 8 10))

(defpage "/" []
    (html5 [:body [:h1 "Welcome to Sweepergame"]
    (form-to [:post "/register"]
     (label "newval" "Your name")
     (text-field "name")
     (submit-button "Register"))])
)


(defpage [:post "/register"] {:as registerobject}
  (dosync 
    (let [playno (inc (@status :numplayers))]
    (ref-set status (assoc @status :numplayers playno
      {:name (registerobject :name) :board (random-board 8 8 10)}))
    (html5 [:body [:h1 "You have code " playno]]))
  )
  
)



(defn board-str [board]
  (str "<html></body><table>"
  (reduce str 
    (map (fn [row] 
      (str "<tr>" (reduce str 
        (map #(str "<td>" (cond (= :bomb %) "X" (= :open %) "1" :else "0") "</td>") row)) 
       "</tr>")
   ) board)) 
   "</table></body></html>")
  )


(defn update-board [board]
  (dosync (ref-set status (assoc @status :board board)))
  )

(defpage "/test" []
  (str ((ring-request) :params))
)

(defn to-int [s]
  (try (Integer/parseInt s) (catch NumberFormatException e nil)))

(defn read-coordinates [req]
  (let [para (req :params)]
  (if (or (nil? para) (nil? (to-int (para :x))) (nil? (to-int (para :y))))
    nil
    [(to-int (para :y)) (to-int (para :x))]
  )))

(defpage "/open" []
;  (str ((ring-request) :params))
  (let [pos (read-coordinates (ring-request))]
  
  (if (nil? pos)
    (str "Need to supply coordinates")
    (let [result (open pos (@status :board))]
    (if (= :error (result :result)) "Error in params"
    (let []
    (update-board (result :board))
    (str "Open returned " (result :result))))
  )))
  )


(defpage "/new" []
  (update-board (random-board 8 8 10))
  (board-str (@status :board))
  )

(defpage "/show" []
  (board-str (@status :board))
  )

(defn -main [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "1337"))
        ]
    (server/start port {:mode mode
                        :ns 'sweepergame})))