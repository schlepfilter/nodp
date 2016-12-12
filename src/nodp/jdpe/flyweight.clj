(ns nodp.jdpe.flyweight)

(def get-engine
  (-> (partial str "StandardEngine ")
      memoize))

(defn- diagnose
  [engine]
  (println "Starting engine diagnostic tool for" engine)
  ;The original implementation was 5000 of sleep.
  (Thread/sleep 500)
  (println "Engine diagnosis complete"))

(map (comp diagnose
           get-engine)
     (concat (repeat 3 1300) (repeat 2 1600)))
