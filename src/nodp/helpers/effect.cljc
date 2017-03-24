(ns nodp.helpers.effect
  (:require [cats.monad.maybe :as maybe]
            [com.rpl.specter :as s]
            [nodp.helpers :as helpers :include-macros true]
            [nodp.helpers.tuple :as tuple])
  #?(:cljs (:require-macros [nodp.helpers.effect :refer [defcurriedmethod]])))

(defmulti call-modifier (comp helpers/get-keyword
                              second
                              vector))

(defn now?
  [e network]
  (maybe/maybe false
               (helpers/get-value e network)
               (comp (partial = (:event (:time network)))
                     tuple/fst)))

#?(:clj (defmacro defcurriedmethod
          [multifn dispatch-val bindings & body]
          `(helpers/defpfmethod ~multifn ~dispatch-val
                                (helpers/curry (fn ~bindings
                                                 ~@body)
                                               ~(count bindings)))))

(defcurriedmethod call-modifier :event
                  [f e network]
                  ;TODO review
                  (if (now? e network)
                    (f (tuple/snd @(helpers/get-value e network)))))

(defn on
  [f entity]
  ;TODO review
  (swap! helpers/network-state
         (partial s/setval*
                  [:effects s/END]
                  [(call-modifier f entity)])))

