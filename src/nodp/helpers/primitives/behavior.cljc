(ns nodp.helpers.primitives.behavior
  (:require [cats.monad.maybe :as maybe]
            [cats.protocols :as p]
            [cats.util :as util]
            [nodp.helpers.tuple :as tuple]
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
                   (helpers/set-latest @parent-behavior child-behavior*)
                   (helpers/make-set-modifier
                     (fn [network]
                       (helpers/set-latest
                         (helpers/get-latest
                           (maybe/maybe parent-behavior
                                        (helpers/get-latest parent-event
                                                            network)
                                        tuple/snd)
                           network)
                         child-behavior*
                         network))
                     child-behavior*))]
    child-behavior))
