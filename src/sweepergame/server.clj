(ns sweepergame.server
  (:use [noir.core]
   [noir.request]
   [noir.response :only [redirect]]
   [sweepergame.core]
   [hiccup.page-helpers :only [html5 link-to  include-js]]
   [hiccup.form-helpers])
  (:require [noir.server :as server]))

(def status (ref {:numplayers 0 :players {}}))
(def debug true)
(def hintsleep 400)
(def opensleep 150)


(def tiles-to-open (count (filter #(= 0 %) (reduce concat (gen-new-board)))))

(def instructions
  [:div [:h2 "Instructions"]
    [:p (str "You must solve a minesweeper game with " board-rows " rows and " board-cols
      " columns with " board-bombs " bombs.")]
    [:p "You can use the following commands"]
    [:ul [:li "To open a cell : http://&lt;server-address&gt;/open?id=&lt;your id&gt;&amp;x=&lt;x (column no starting on 0)&gt;&amp;y=&lt;y (row)&gt;"]
     [:li "Let the server open a cell for you - http://&lt;server-address&gt;/open?id=&lt;your id&gt;&amp;x=&lt;x (column no starting on 0)&gt;&amp;y=&lt;y (row)&gt;"]
     [:li "Check how you board looks like now (used for debug) http://&lt;server-address&gt;/debugdisplay?id=&lt;your id&gt;"]
    ]
    [:p "You will receive an answer like this:"]
    [:p "'Y=3,X=5,result=4'"]  
    [:p "This indicates that the sixth column on the fourth row has four bombs. The cell in the upper left corner has coordinate 0,0"]
    [:p "If you get a result like :bomb or :open it means that you have opened a bomb or an already open field. Your score will then be set to 0 and a new board will be generated for you to start over"]  
    [:p "You get points for each finished board. Fewer hints used means more points. If you open a mine or a cell that is already opened your score will be set to zero"]
    [:p "When you finish a board (or a mine blows up) a new board will be generated automatically"]  
    ]
  )

(defn sort-by-score [player-values]
  (sort (fn [val1 val2] (compare ((val1 :points) :total) ((val2 :points) :total))) 
       player-values)
  )


(defn html-encode [x]
  (-> x 
    (.replaceAll x "&" "&amp;") 
    (.replaceAll ">" "&gt;")
    (.replaceAll "<" "&lt;")
    (.replaceAll "æ" "&aelig;")
    (.replaceAll "Æ" "&Aelig;")
    (.replaceAll "ø" "&oslash;")
    (.replaceAll "Ø" "&Oslash;")
    (.replaceAll "å" "&aring;")
    (.replaceAll "Å" "&Åring;")
    
  ))

(defn scoretable []
  [:div {:id "scoreboard"}
    [:table {:border 1}
      [:tr [:th "Name"] [:th "Score"] [:th "Finished boards"] [:th "Max opens on one board"]]
      (map (fn [player-map] [:tr 
        [:td (html-encode (player-map :name))] 
        [:td ((player-map :points) :total)]
        [:td ((player-map :points) :finishedBoards)]
        [:td ((player-map :points) :maxOnBoard)]
        ]) (sort-by-score (vals (@status :players))))
      ]
    
    ]
)

(defpage "/showscoretable" []
  (html5 (scoretable))
  )

(defn show-scoreboard []
  [:p
    (scoretable)
    instructions
    ]
  )

(defpage "/" []
    (html5 
       [:head
    [:title "Sweepergame"]
    (include-js "/jquery-1.7.2.js") (include-js "/reload.js")]
      [:body [:h1 "Welcome to Sweepergame"]
    (form-to [:post "/register"]
     (label "name" "Your name")
     (text-field {:maxlength 20} "name")
     (submit-button "Register"))
    [:p (show-scoreboard)]])
)

(defn new-playno [playno]
  (if debug playno
  (+ (* (+ (rand-int 8000) 1000) 100) (+ playno 10)))
  )

(defpage [:post "/register"] {:as registerobject}
  (dosync 
    (let [playno (new-playno (inc (@status :numplayers)))]
    (ref-set status (assoc @status 
      :numplayers (inc (@status :numplayers))
      :players (assoc (@status :players)
      (str playno)
      {:name (registerobject :name) :board (gen-new-board) :points {:total 0 :finishedBoards 0 :maxOnBoard 0}})))
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





(defn to-int [s]
  (try (Integer/parseInt s) (catch NumberFormatException e nil)))

(defn read-coordinates [para]  
  (if (nil? para) nil
  (let [x (if (nil? (para :x)) (para :X) (para :x)) 
        y (if (nil? (para :y)) (para :Y) (para :y))]
  (if (or (nil? (to-int x)) (nil? (to-int y)))
    nil
    [(to-int y) (to-int x)]
  ))))

(defn in-third [x]
  (* x x x)
  )

(def tiles-to-open (- (* board-rows board-cols) board-bombs))
(def max-score (in-third tiles-to-open))

(defn calc-score [type-of-open open-res board old-score]  
  (if (or (= open-res :open) (= open-res :bomb)) (assoc old-score :total 0)
  (let [move-score 
    (if (= :open type-of-open) (let [num-open (number-of-opens board)] 
     (- (in-third num-open) (if (> num-open 0) (in-third (dec num-open)) 0))
    ) 0)] 
  (assoc old-score :total (+ (old-score :total) move-score)
   :finishedBoards (if (finished? board) (inc (old-score :finishedBoards)) (old-score :finishedBoards))
   :maxOnBoard (max (old-score :maxOnBoard) (number-of-opens board)))
   ))
)

(defpage [:get "/open"] {:as openpart}
  (let [player-map ((@status :players) (openpart :id)) pos (read-coordinates openpart)]
  (cond 
    (nil? player-map) "Unknown player"
    (nil? pos) "Coordinates needed"
    :else (let [result (open pos (player-map :board))]
  (let [score (calc-score :open (result :result) (result :board) (player-map :points))]
    (dosync (ref-set status (assoc @status 
      :players (assoc (@status :players) 
    (openpart :id) (assoc player-map :points score :board (replace-if-finished result))))))
    (Thread/sleep opensleep)  
    (str "Y=" (first (result :pos)) ",X=" (second (result :pos)) ",result=" (result :result))
  ))
  )
))

(defpage [:get "/hint"] {:as openpart}
  (let [player-map ((@status :players) (openpart :id))]
  (if 
    (nil? player-map) "Unknown player"
    (let [result (hint (player-map :board))]
    (dosync (ref-set status (assoc @status 
      :players (assoc (@status :players) 
    (openpart :id) (assoc player-map :board (replace-if-finished result) :points (calc-score :hint (result :result) (result :board) (player-map :points)))))))
    (Thread/sleep hintsleep)  
    (str "Y=" (first (result :result)) ",X=" (second (result :result)) ",result=" (result :count))
  )))
  )

(defn board-as-html [board]
  [:table
  (map (fn [row] [:tr (map (fn [cell] [:td 
    (cond (= :bomb cell) (if debug "X" "0")
          (= :open cell) "1" 
          (= :hint cell) "h"
          :else "0")]) row)]) board)
  ]
)

(defpage [:get "/debugdisplay"] {:as idpart}
  (html5 
    [:head
    [:title "Sweepergame"]
    (include-js "/jquery-1.7.2.js") (include-js "/reload.js")]
    [:body
    (let [player-map ((@status :players) (idpart :id))]
    (if (nil? player-map) "Unknown player"
    (let [player-board (player-map :board) player-score (player-map :points)]
      (board-as-html (player-map :board))
    )))
    ]
))

(defn -main [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "1337"))
        ]
    (server/start port {:mode mode
                        :ns 'sweepergame})))