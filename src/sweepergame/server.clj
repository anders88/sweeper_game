(ns sweepergame.server
  (:use [noir.core]
   [noir.request]
   [noir.response :only [redirect]]
   [sweepergame.core]
   [hiccup.page-helpers :only [html5 link-to]]
   [hiccup.form-helpers])
  (:require [noir.server :as server]))

(def status (ref {:numplayers 0 :players {}}))
(def debug true)

(defn gen-new-board [] (random-board 8 8 10))
(def tiles-to-open (count (filter #(= 0 %) (reduce concat (gen-new-board)))))

(defn  show-scoreboard []
  [:div {:id "scoreboard"}
    [:table {:border 1}
      [:tr [:th "Name"] [:th "Score"]]
      (map (fn [player-map] [:tr [:td (player-map :name)] [:td (player-map :points)]]) (vals (@status :players)))
      ]
    
    ]
  )

(defpage "/" []
    (html5 [:body [:h1 "Welcome to Sweepergame"]
    (form-to [:post "/register"]
     (label "newval" "Your name")
     (text-field "name")
     (submit-button "Register"))
    [:p (show-scoreboard)]])
)

(defn new-playno [playno]
  (+ (* (+ (rand-int 8000) 1000) 100) (+ playno 10))
  )

(defpage [:post "/register"] {:as registerobject}
  (dosync 
    (let [playno (new-playno (inc (@status :numplayers)))]
    (ref-set status (assoc @status 
      :numplayers (inc (@status :numplayers))
      :players (assoc (@status :players)
      (str playno)
      {:name (registerobject :name) :board (gen-new-board) :points 0})))
    (html5 [:body [:h1 "You have code " playno]
           [:p (link-to "/" "Scoreboard")]]))
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

(defn board-as-html [board]
  [:table
  (map (fn [row]
  ]
)



(defn to-int [s]
  (try (Integer/parseInt s) (catch NumberFormatException e nil)))

(defn read-coordinates [para]  
  (if (or (nil? para) (nil? (to-int (para :x))) (nil? (to-int (para :y))))
    nil
    [(to-int (para :y)) (to-int (para :x))]
  ))

(defn calc-score [open-res board old-score]
  (cond (or (= open-res :open) (= open-res :board)) 0
  :else (inc old-score)
  )
  )

(defpage [:get "/open"] {:as openpart}
  (let [player-map ((@status :players) (openpart :id)) pos (read-coordinates openpart)]
  (cond 
    (nil? player-map) "Unknown player"
    (nil? pos) "Coordinates needed"
    :else (let [result (open pos (player-map :board))]
  (let [score (calc-score (result :result) (result :board) (player-map :points))]
    (dosync (ref-set status (assoc @status 
      :players (assoc (@status :players) 
    (openpart :id) (assoc player-map :points score :board (result :board))))))
    (str (result :result))
  ))
  )
))

(defpage [:get "/debugdisplay"] {:as idpart}
  (html5 [:body
  (if debug 
    (let [player-map ((@status :players) (openpart :id))]
      
  

(defn -main [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "1337"))
        ]
    (server/start port {:mode mode
                        :ns 'sweepergame})))