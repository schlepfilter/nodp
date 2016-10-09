(ns hfdp.strategy)

(defmulti perform identity)

(defmethod perform [:mullard :quack]
  [& _]
  (println "quack"))

(perform [:mullard :quack])
