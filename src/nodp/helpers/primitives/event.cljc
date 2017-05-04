(ns nodp.helpers.primitives.event
  (:require [cats.protocols :as p]
            [nodp.helpers :as helpers]))

(declare context)

(defn event*
  ;TODO return Event
  [fs])

(def context
  (helpers/reify-monad
    ;TODO implement monad
    (fn [])
    (fn [])
    ;TODO implement semigroup
    ;TODO implement monoid
    p/Monoid
    (-mempty [_]
             (event* []))))