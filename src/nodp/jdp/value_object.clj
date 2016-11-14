(ns nodp.jdp.value-object
  (:require [clojure.string :as str]))

(def stat-a
  {:strength     10
   :intelligence 5
   :lack         0})

(def stat-b
  stat-a)

(def stat-c
  {:strength     5
   :intelligence 1
   :lack         8})

(defmacro compare-stats
  [x y]
  `(str/join " " ["Is" '~x "and" '~y "equal :" (= ~x ~y)]))

(defmacro compare-stats-collection
  ([]
    '())
  ([x y & more]
   `(cons (compare-stats ~x ~y)
          (compare-stats-collection ~@more))))

(compare-stats-collection stat-a stat-b
                          stat-a stat-c)
