(ns nodp.helpers.derived.event.clojure.core
  (:refer-clojure :exclude [drop reduce])
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
        (drop 1)
        (helpers/<$> :event-value)))
  ([f val e]
   (event/transduce (clojure.core/drop 0) f val e)))
