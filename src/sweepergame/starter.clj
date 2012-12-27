(ns sweepergame.starter
	(:use [sweepergame.server :only [startup]]
		   [clojure.java.io]
		   [noir.util.crypt :only [encrypt]])
)

(defn keyval [x]
  (let [pair (clojure.string/split x #"=")] [(keyword (first pair)) (second pair)])
  )

(defn read-enviroment-variables [args]
  (if (and (second args) (.exists (new java.io.File (second args))))
    (apply hash-map (flatten (map keyval (clojure.string/split-lines (slurp (second args))))))
    (let [res {:mode :dev :secured false :password-file "password.txt" :rows "16" :cols "30" :bombs "99" :hintsleep "400" :opensleep "150"}]
    (println "Did not find setupfile. Using standard setup. Use 'lein run start <setupfile>' to supply a setupfile. Running with admin disabled - you must restart server manually")
    res)

  )
  )

(defn conv-to-int [envmap paraname]
	(assoc envmap paraname (Integer/parseInt (envmap paraname)))
	)

(defn read-env-add-defaults [args]
	(assoc (-> (read-enviroment-variables args) (conv-to-int :rows) (conv-to-int :cols) (conv-to-int :bombs) (conv-to-int :hintsleep) (conv-to-int :opensleep)) :allow-reopen false)
	)

(defn update-password-file [enviroment-read]
	(println "Type new password:")
	(let [new-password (read-line)]
	(with-open [wrtr (writer (enviroment-read :password-file))]
  		(.write wrtr (encrypt new-password)))
	(println "Password file updated")
	)

)

(defn -main [& m]
	(let [enviroment-read (read-env-add-defaults m)]
		(println enviroment-read)
		(if (= "setPassword" (first m))
			(update-password-file enviroment-read)
			(startup enviroment-read)
			)
		)
)