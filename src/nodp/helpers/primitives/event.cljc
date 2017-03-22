(ns nodp.helpers.primitives.event
  (:require [cats.monad.maybe :as maybe]
            [cats.protocols :as p]
            [cats.util :as util]
            [nodp.helpers :as helpers]
            [com.rpl.specter :as s])
  #?(:clj
           (:import (clojure.lang IDeref))
     :cljs (:require-macros [nodp.helpers.primitives.event :refer [event*]])))

(defrecord Event
  [id]
  p/Printable
  (-repr [_]
    (str "#[event " id "]"))
  IDeref
  (#?(:clj  deref
      :cljs -deref) [e]
    (helpers/get-value e @helpers/network-state)))

(util/make-printable Event)

(helpers/defcurried get-start
  [entity network]
  ((:id entity) (:start network)))

(defn if-then-else
  [if-function then-function else]
  (if (if-function else)
    (then-function else)
    else))

(helpers/defcurried set-start
                    [entity a network]
                    (if-then-else (comp maybe/nothing? (get-start entity))
                      (partial s/setval* [:start (:id entity)] a)
                      network))

(defn make-set-start-value
  [entity a]
  (comp (helpers/set-value entity a)
        (set-start entity a)))

#?(:clj (defmacro event*
          [entity-name & fs]
          `(helpers/get-entity ~entity-name
                               Event.
                               ~@fs
                               (make-set-start-value ~entity-name
                                                     (maybe/nothing)))))

(defn event
  []
  (event* e))
