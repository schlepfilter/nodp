(ns nodp.helpers.window
  (:require [com.rpl.specter :as s]
            [nodp.helpers.primitives.behavior :as behavior]))

(declare inner-height)

(declare inner-width)

(->> (behavior/get-defs inner-height inner-width)
     (partial s/setval* s/END)
     (swap! behavior/defs))
