(ns sweepergame.server
  (:use [noir.core]
   [noir.request]
   [noir.response :only [redirect]]
   [sweepergame.core]
   [hiccup.page-helpers :only [html5 link-to  include-js]]
   [hiccup.form-helpers]
   [cheshire.core :only [generate-string]]
   )
  (:require [noir.server :as server]))

(def starting-state {:numplayers 0 :players {}})
(def status (ref starting-state))
(def enviroment (ref {}))
(def debug false)
(def hintsleep 400)
(def opensleep 150)
(def session-length 120)

(defn player-object [player-no]
  ((@status :players) (str "" player-no)))

(defn new-playno [playno]
  (if debug playno
  (+ (* (+ (rand-int 8000) 1000) 100) (+ playno 10)))
  )


(defn register-new-player [new-player-name]
  (dosync
   (let [playno (new-playno (inc (@status :numplayers)))]

     (ref-set status (assoc @status
                       :numplayers (inc (@status :numplayers))
                       :players (assoc (@status :players)
                                  (str playno)
                                  {:name new-player-name :board (gen-new-board) :points {:total 0 :finishedBoards 0 :maxOnBoard 0 :bombed 0}})))
     playno
     )
   )

)

(defn in-third [x]
  (* x x x)
  )


(defn calc-score
  [type-of-open open-res board old-score given-move-score]
  (if (or (= open-res :open) (= open-res :bomb)) (assoc old-score :total 0 :bombed (inc (old-score :bombed)))
  (assoc old-score :total (+ (old-score :total) given-move-score)
   :finishedBoards (if (finished? board) (inc (old-score :finishedBoards)) (old-score :finishedBoards))
   :maxOnBoard (max (old-score :maxOnBoard) (number-of-opens board)))
   )
)

(defn move-score [board]
  (let [num-open (number-of-opens board)]
     (- (in-third num-open) (if (> num-open 0) (in-third (dec num-open)) 0))
    )
)


(defn update-player
  ([result player-no player-map] (update-player result player-no player-map 0))
  ([result player-no player-map given-move-score]
  (dosync (ref-set status (assoc @status
                            :players (assoc (@status :players)
                                       player-no (assoc player-map :board (replace-if-finished result)
                                                        :points (calc-score :hint (result :result) (result :board) (player-map :points) given-move-score)))))
          )))

(defn all-player-objects[]
 (vals (@status :players))
)



(def tiles-to-open (count (filter #(= 0 %) (reduce concat (gen-new-board)))))

(defn sort-by-score [player-values]
  (sort-by :points (fn [val1 val2] (* -1 (compare (val1 :total) (val2 :total))))
       player-values)
  )


(defn html-encode [x]
  (-> x
    (.replaceAll "&" "&amp;")
    (.replaceAll ">" "&gt;")
    (.replaceAll "<" "&lt;")
    (.replaceAll "æ" "&aelig;")
    (.replaceAll "Æ" "&Aelig;")
    (.replaceAll "ø" "&oslash;")
    (.replaceAll "Ø" "&Oslash;")
    (.replaceAll "å" "&aring;")
    (.replaceAll "Å" "&Åring;")

  ))




(defpage [:post "/register"] {:as registerobject}
  (let [playno (register-new-player (registerobject :name))]
    (redirect (str "/deb?id=" playno))
))




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


(def tiles-to-open (- (* board-rows board-cols) board-bombs))
(def max-score (in-third tiles-to-open))




(defpage [:get "/open"] {:as openpart}
  (let [player-map (player-object (openpart :id)) pos (read-coordinates openpart)]
  (cond
    (nil? player-map) "Unknown player"
    (nil? pos) "Coordinates needed"
    :else (let [result (open pos (player-map :board) (@enviroment :allow-reopen))]
  (let [score (move-score (result :board))]
    (update-player result (openpart :id) player-map score)
    (Thread/sleep opensleep)
    (str "Y=" (first (result :pos)) ",X=" (second (result :pos)) ",result=" (result :result))
  ))
  )
))



(defpage [:get "/hint"] {:as openpart}
  (let [player-map (player-object (openpart :id))]
  (if
    (nil? player-map) "Unknown player"
    (let [result (hint (player-map :board))]
    (update-player result (openpart :id) player-map)
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
  (redirect (str "/debugoutput.html?id=" (idpart :id)))
)

(defpage [:get "/"] {:as nopart}
  (redirect "/index.html")
)

(defpage [:get "/deb"] {:as idpart}
  (redirect (str "/debugoutput.html?id=" (idpart :id)))
)



(defn gen-json-score [scores]
  (generate-string (map (fn [val]
                          {:name (val :name)
                           :total ((val :points) :total)
                           :finishedBoards ((val :points) :finishedBoards)
                           :maxOnBoard ((val :points) :maxOnBoard)
                           :bombed ((val :points) :bombed)}
                          ) scores)
                   )
)

(defpage [:get "/scorejson"] {:as nopart}
  (gen-json-score (sort-by-score (all-player-objects)))
)

(defpage [:get "/boarddebug"] {:as idpart}
  (let [player-map (player-object (idpart :id))]
    (if (nil? player-map) "Unknown board"
        (generate-string (debug-board (player-map :board))))))


(defpage [:get "/login"] {:as nopart}
  (redirect "/login.html")
)

(defpartial page-header[] 
  [:head 
  [:link {:href "css/bootstrap.min.css" :rel "stylesheet"}]
  [:script {:src "jquery-1.7.2.js"}]
  [:script {:src "js/bootstrap.min.js"}]
    ]
  )

(defpartial errorpage [message]
  (html5
    (page-header)
    [:body
      [:h1 message]      
      [:p (link-to "/" "Return to main")]
        ]
    )    
  
  )


(defpage [:post "/doLogin"] {:as login-post}
  (if (noir.util.crypt/compare (login-post :password) (slurp (@enviroment :password-file)))
  (let [session-expires (.plusSeconds (new org.joda.time.DateTime) session-length)]
    (noir.session/put! :sess-end session-expires)
    (redirect "/admin")
    )
  (errorpage "Error logging in")    
))


(defn logged-in? []
  (.isBefore (new org.joda.time.DateTime) (noir.session/get :sess-end (.minusSeconds (new org.joda.time.DateTime) 100)))
  )

(defpage [:get "/admin"] {:as nopart}
  (cond (logged-in?)
  (html5
    (page-header)
    [:body
      [:h1 "Adminpage"]
      (form-to [:post "/resetGame"]
        (label "resetText" "Type reset to reset game")
        (text-field "resetText")
        (submit-button "Reset")
        )
      [:h2 "Game rules"]
      [:p (link-to "/" "Return to main")]
    ]    
    )
    (@enviroment :secured) (redirect "/login")
    :else (errorpage "You must run with secured server to use adminpage (See Administrative functions in help)")
    ))

(defpage [:post "/resetGame"] {:as resetpart}
  (cond 
    (not (logged-in?)) (errorpage "Not logged in")
    (not (= (resetpart :resetText) "reset")) (errorpage "Please type reset in textfield to confirm")
    :else (dosync 
      (ref-set status starting-state)
      (html5 [:body [:h1 "Game restarted"]])
      )
    )
  )


(defn startup [supplied-enviroment]
   (dosync (ref-set enviroment supplied-enviroment))
  
  (let [mode (@enviroment :mode)
        port (Integer. (get (System/getenv) "PORT" "8080"))
        ]
    (server/start port {:mode mode
                        :ns 'sweepergame}))
)