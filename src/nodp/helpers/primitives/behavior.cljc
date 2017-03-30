(ns nodp.helpers.primitives.behavior
  (:require [cats.protocols :as p]
            [nodp.helpers :as helpers])
  #?(:clj
           (:import (clojure.lang IDeref))
     :cljs (:require-macros [nodp.helpers.primitives.behavior :refer [behavior*]])))

(declare context)

(defrecord Behavior
  [id]
  p/Contextual
  (-get-context [_]
    context)
  IDeref
  (#?(:clj  deref
      :cljs -deref) [b]
    (helpers/get-latest b @helpers/network-state)))

#?(:clj (defmacro behavior*
          [event-name & fs]
          `(helpers/get-entity ~event-name
                               Behavior.
                               ~@fs)))

(def context
  (helpers/reify-monad
    (fn [a]
      (behavior* b
                 (helpers/set-latest a b)))
    (fn [ma f])))
