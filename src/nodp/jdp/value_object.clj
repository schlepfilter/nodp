(ns nodp.jdp.value-object
  (:require [clojure.string :as str]
            [nodp.helpers :as helpers]))

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

(compare-stats stat-a stat-b)

(compare-stats stat-a stat-c)
