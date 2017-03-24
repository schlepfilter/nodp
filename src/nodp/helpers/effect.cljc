(ns nodp.helpers.effect
  (:require [cats.monad.maybe :as maybe]
            [com.rpl.specter :as s]
            [nodp.helpers :as helpers]
            [nodp.helpers.primitives.event :as event]
            [nodp.helpers.tuple :as tuple]))

(defmulti make-call-modifier (comp helpers/get-keyword
                                   second
                                   vector))

(defn now?
  [e network]
  (maybe/maybe false
               (helpers/get-value e network)
               (comp (partial = (:event (:time network)))
                     tuple/fst)))

(defmethod make-call-modifier :event
  [f e]
  ;TODO review
  (fn [network]
    (if (now? e network)
      (f (tuple/snd @(helpers/get-value e network))))))

(defn on
  [f entity]
  ;TODO review
  (swap! helpers/network-state
         (partial s/setval*
                  [:effects s/END]
                  [(make-call-modifier f entity)])))

