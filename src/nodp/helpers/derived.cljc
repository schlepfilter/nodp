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

(defn get-occs-or-latests-coll
  [initial ids network]
  (map (partial (helpers/flip (event/make-get-occs-or-latests initial))
                network)
       ids))

(defn make-combine-occs-or-latests
  [f]
  (comp (helpers/build tuple/tuple
                       (comp tuple/fst first)
                       (comp (partial apply f)
                             (partial map tuple/snd)))
        vector))

(def make-combine-occs-or-latests-coll
  (comp (helpers/curry 2 apply)
        (helpers/curry 3 map)
        make-combine-occs-or-latests))

(helpers/defcurried modify-combine
                    [f parents initial child network]
                    (event/set-occs ((make-combine-occs-or-latests-coll f)
                                      (get-occs-or-latests-coll initial
                                                                parents
                                                                network))
                                    child
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
