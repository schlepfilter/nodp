(ns nodp.helpers.primitives.behavior
  (:require [cats.protocols :as p]
            [cats.util :as util]
            [nodp.helpers :as helpers])
  #?(:clj
           (:import (clojure.lang IDeref))
     :cljs (:require-macros [nodp.helpers.primitives.behavior
                             :refer
                             [behavior*]])))

(declare context)

(defrecord Behavior
  [id]
  p/Contextual
  (-get-context [_]
    context)
  IDeref
  (#?(:clj  deref
      :cljs -deref) [b]
    (helpers/get-latest b @helpers/network-state))
  p/Printable
  (-repr [_]
    (str "#[behavior " id "]")))

#?(:clj (defmacro behavior*
          [event-name & fs]
          `(helpers/get-entity ~event-name
                               Behavior.
                               ~@fs)))

(util/make-printable Behavior)

(def context
  (helpers/reify-monad
    (fn [a]
      (behavior* b
                 (helpers/set-latest a b)))
    (fn [ma f])))

(defn switcher
  [parent-behavior parent-event]
  (let [child-behavior
        (behavior* child-behavior*
                   (helpers/set-latest @parent-behavior child-behavior*))]
    child-behavior))
