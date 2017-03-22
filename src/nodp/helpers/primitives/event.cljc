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

(defn get-start
  [entity network]
  ((:id entity) (:start network)))

(helpers/defcurried set-start
  [entity a network]
  (if (maybe/nothing? (get-start entity network))
    (s/setval [:start (:id entity)] a network)
    network))

(defn make-set-start-value
  [entity a]
  (comp (helpers/set-value entity a) (set-start entity a)))

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
