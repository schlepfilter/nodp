(ns nodp.helpers.window
  (:require [nodp.helpers :as helpers]
            [nodp.helpers.primitives.behavior :as behavior]))

(declare inner-width)

(defn def-inner-width
  []
  (def inner-width
    (behavior/behavior* b
                        (helpers/set-modifier
                          (helpers/set-latest
                            js/window.innerWidth
                            b)))))

(swap! behavior/defs (partial (helpers/flip conj) def-inner-width))

