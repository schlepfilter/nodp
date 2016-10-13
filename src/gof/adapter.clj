(ns gof.adapter)

(def turkey-quack
  (partial println "Gobble gobble"))

(def duck-quack
  turkey-quack)

(def turkey-fly
  (partial println "I'm flying a short distance"))

(defn duck-fly
  []
  (dotimes [_ 5] (turkey-fly)))

(duck-quack)

(duck-fly)
