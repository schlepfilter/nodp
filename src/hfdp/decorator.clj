(ns hfdp.decorator
  (:require [clojure.string :as str]))

(def coffee
  {:dark-roast {:name  "Dark Roast Coffee"
                :price 0.99}})

(def condiment
  {:mocha {:name  "Mocha"
           :price 0.2}
   :whip  {:name  "Whip"
           :price 0.1}})

;TODO handle the case where the types of first and second elements differ
(defmulti add (comp type first vector))

(defmethod add String
  [& ss]
  (str/join ", " ss))

(defmethod add Number
  [& ns]
  (apply + ns))

(merge-with add
            (:dark-roast coffee)
            (:mocha condiment))

