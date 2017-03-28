(ns nodp.helpers.effect
  (:require [cats.core :as m]
            [cats.monad.maybe :as maybe]
            [com.rpl.specter :as s]
            [nodp.helpers :as helpers :include-macros true]
            [nodp.helpers.primitives.event :as event]
            [nodp.helpers.tuple :as tuple])
  #?(:cljs (:require-macros [nodp.helpers.effect :refer [defcurriedmethod]])))

(defmulti call-modifier (comp helpers/get-keyword
                              second
                              vector))

(defn now?
  [e network]
  (maybe/maybe false
               (helpers/get-latest e network)
               (comp (partial = (:event (:time network)))
                     tuple/fst)))

#?(:clj (defmacro defcurriedmethod
          [multifn dispatch-val bindings & body]
          `(helpers/defpfmethod ~multifn ~dispatch-val
                                (helpers/curry ~(count bindings)
                                               (fn ~bindings
                                                 ~@body)))))

(defcurriedmethod call-modifier :event
                  [f e network]
                  (if (now? e network)
                    (f (event/get-value e network))))

(def on
  (comp (partial swap! helpers/network-state)
        ((m/curry s/setval*) [:effects s/END])
        vector
        call-modifier))
