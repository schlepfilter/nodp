(ns nodp.helpers.window
  (:require [com.rpl.specter :as s]
            [cuerdas.core :as cuerdas]
            [nodp.helpers :as helpers]
            [nodp.helpers.primitives.behavior :as behavior])
  #?(:cljs (:require-macros [nodp.helpers.window :refer [get-defs]])))

(declare inner-height)

(declare inner-width)

#?(:clj (defmacro get-defs
          [& symbols]
          (mapv (fn [x]
                  `(fn []
                     (def ~x
                       (behavior/behavior* b#
                                           (helpers/set-modifier
                                             (helpers/set-latest
                                               ~(->> x
                                                     cuerdas/camel
                                                     (str "js/window.")
                                                     symbol)
                                               b#))))))
                symbols)))

#?(:cljs (->> (get-defs inner-height inner-width)
              (partial s/setval* s/END)
              (swap! behavior/defs)))
