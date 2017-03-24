(ns nodp.helpers.effect
  (:require [cats.monad.maybe :as maybe]
            [com.rpl.specter :as s]
            [nodp.helpers :as helpers]
            [nodp.helpers.primitives.event :as event]
            [nodp.helpers.tuple :as tuple]))

(defmulti make-call-modifier (comp helpers/get-keyword
                                   second
                                   vector))

(defmethod make-call-modifier :event
  [f entity]
  (fn [network]
    (if (and (maybe/just? (helpers/get-value entity network))
             (= (:event (:time network))
                (event/get-time entity network)))
      (f (tuple/snd @(helpers/get-value entity network))))))

(defn on
  [f entity]
  (swap! helpers/network-state
         (partial s/setval*
                  [:effects s/END]
                  [(make-call-modifier f entity)])))

