(ns hfdp.adapter)

(defn- turkey-quack
  []
  (println "Gobble gobble"))

(def duck-quack
  turkey-quack)

(defn- turkey-fly
  []
  (println "I'm flying a short distance"))

(defn duck-fly
  []
  (dotimes [_ 5] (turkey-fly)))

(duck-quack)

(duck-fly)
