(ns nodp.helpers.derived
  (:require [cats.context :as ctx]
            [nodp.helpers :as helpers]
            [nodp.helpers.clojure.core :as core]
            [nodp.helpers.primitives.behavior :as behavior]
            [nodp.helpers.primitives.event :as event]
            [nodp.helpers.tuple :as tuple]))

(helpers/defcurried add-edges
                    [parents child network]
                    (helpers/call-functions (map ((helpers/flip event/add-edge)
                                                   child)
                                                 parents)
                                            network))

(defn get-occs-or-latests
  [initial parent-events network]
  (map (partial (helpers/flip (event/make-get-occs-or-latests initial))
                network)
       parent-events))

(helpers/defcurried modify-combine
                    [f parent-events initial child-event network]
                    (event/set-occs (map tuple/tuple (map tuple/fst (first (get-occs-or-latests initial parent-events network)))
                                         (apply (partial map f) (map (partial map tuple/snd)
                                                                     (get-occs-or-latests initial parent-events network))))
                                    child-event
                                    network))
(defn combine
  [f & parent-events]
  ((helpers/build (comp event/event*
                        cons)
                  add-edges
                  (comp event/make-set-modify-modify
                        (modify-combine f)))
    (map :id parent-events)))

(def mean
  (helpers/build (partial combine /)
                 core/+
                 core/count))

(defn behavior
  [a]
  (->> a
       nodp.helpers/pure
       (ctx/with-context behavior/context)))

(def switcher
  (comp helpers/join
        behavior/stepper))
