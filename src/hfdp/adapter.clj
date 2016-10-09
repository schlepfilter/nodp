(ns hfdp.adapter)

(defmulti perform vector)

(defmacro defperform
  [object action expr]
  `(defmethod perform [~object ~action]
     [& _#]
     ~expr))

(defperform :duck :quack (println "Gobble gobble"))

(defn- fly
  []
  (dotimes [_ 5] (println "I'm flying a short distance")))

(defperform :duck :fly (fly))

(perform :duck :quack)

(perform :duck :fly)
