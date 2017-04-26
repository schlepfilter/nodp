(ns nodp.helpers.derived.event
  (:refer-clojure :exclude [+ count drop max min reduce])
  (:require [nodp.helpers.primitives.event :as event]
            [nodp.helpers.unit :as unit]))

(def reduce
  (partial event/transduce (clojure.core/drop 0)))

(def max
  (partial reduce clojure.core/max #?(:clj  Double/NEGATIVE_INFINITY
                                      :cljs js/Number.NEGATIVE_INFINITY)))

(def min
  (partial reduce clojure.core/min #?(:clj  Double/POSITIVE_INFINITY
                                      :cljs js/Number.POSITIVE_INFINITY)))

(def +
  (partial reduce clojure.core/+ 0))

(def count
  (partial event/transduce (map (constantly 1)) clojure.core/+ 0))
