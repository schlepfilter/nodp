(ns gof.strategy)

(def quack
  (partial println "Quack"))

(def fly-rocket-powered
  (partial println "I'm flying with a rocket"))

(def fly-no-way
  (partial println "I can't fly"))

(defn funcall
  [f]
  (f))

(funcall quack)

(funcall fly-no-way)

(funcall fly-rocket-powered)
