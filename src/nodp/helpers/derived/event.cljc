(ns nodp.helpers.derived.event
  (:refer-clojure :exclude [max min])
  (:require [nodp.helpers.primitives.event :as event]))

(def max
  (partial event/transduce
           (map identity)
           clojure.core/max
           Double/NEGATIVE_INFINITY))

(def min
  (partial event/transduce
           (map identity)
           clojure.core/min
           Double/POSITIVE_INFINITY))