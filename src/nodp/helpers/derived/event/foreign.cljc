(ns nodp.helpers.derived.event.foreign
  (:require [nodp.helpers :as helpers]
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
  (event/event* (cons (add-edges (map :id parent-events))
                      (event/make-set-modify-modify (modify-combine f (map :id parent-events))))))
