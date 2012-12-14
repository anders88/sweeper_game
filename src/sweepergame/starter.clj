(ns sweepergame.starter
	(:use [sweepergame.server :only [startup]]
		   [clojure.java.io])
)

(defn keyval [x]
  (let [pair (clojure.string/split x #"=")] [(keyword (first pair)) (second pair)])
  )

(defn read-enviroment-variables [args]
  (if (and (second args) (.exists (new java.io.File (second args))))
    (apply hash-map (flatten (map keyval (clojure.string/split-lines (slurp (second args))))))
    (let [res {:mode :dev :secured false :password-file "password.txt"}]
    (println "Did not find setupfile. Using standart setup. Use 'lein run <setupfile>' to supply a setupfile. Running with admin disabled - server must restart manually")
    res)

  )
  )

(defn update-password-file [enviroment-read]
	(with-open [wrtr (writer (enviroment-read :password-file))]
  		(.write wrtr "Line to be written"))
	(println "Updating password")
)

(defn -main [& m]
	(let [enviroment-read (read-enviroment-variables m)]
		(if (= "setPassword" (first m))
			(update-password-file enviroment-read)
			(startup enviroment-read)
			)
		)
)