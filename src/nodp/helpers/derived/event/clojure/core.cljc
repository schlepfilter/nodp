(ns nodp.helpers.derived.event.clojure.core
  (:refer-clojure :exclude [drop max reduce])
  (:require [nodp.helpers :as helpers]
            [nodp.helpers.primitives.event :as event]
            [nodp.helpers.unit :as unit]))

(defn drop
  [n e]
  (event/transduce (clojure.core/drop n)
                   (comp second
                         vector)
                   e))

(defn reduce
  ([f e]
   (->> e
        (event/transduce (clojure.core/drop 0)
                         (fn [{:keys [event-value start]} element]
                           {:event-value (if start
                                           element
                                           (f event-value element))
                            :start       false})
                         {:event-value unit/unit
                          :start       true})
        (helpers/<$> :event-value)))
  ([f val e]
   (event/transduce (clojure.core/drop 0) f val e)))

(def max
  (partial reduce clojure.core/max Double/NEGATIVE_INFINITY))
