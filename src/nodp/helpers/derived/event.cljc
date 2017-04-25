(ns nodp.helpers.derived.event
  (:refer-clojure :exclude [max min reduce])
  (:require [nodp.helpers.primitives.event :as event]))

(defn reduce
  [f val e]
  (event/transduce (drop 0) f val e))

(def max
  (partial reduce clojure.core/max Double/NEGATIVE_INFINITY))

(def min
  (partial reduce clojure.core/min Double/POSITIVE_INFINITY))