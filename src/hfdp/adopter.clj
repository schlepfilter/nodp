(ns hfdp.adopter)

(defmulti perform identity)

(defmethod perform [:duck :quack]
  [& _]
  (println "Gobble gobble"))

(perform [:duck :quack])
