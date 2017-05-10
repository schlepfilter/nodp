(ns nodp.helpers.derived.event.foreign
  (:require [nodp.helpers :as helpers]
            [nodp.helpers.primitives.event :as event]))

(helpers/defcurried add-edges
                    [parents child network]
                    (helpers/call-functions (map ((helpers/flip event/add-edge)
                                                   child)
                                                 parents)
                                            network))

(defn combine
  [f & parent-events]
  (event/event* [(add-edges (map :id parent-events))]))
