(ns nodp.helpers.primitives.event
  (:require [cats.monad.maybe :as maybe]
            [cats.protocols :as p]
            [cats.util :as util]
            [com.rpl.specter :as s]
            [nodp.helpers :as helpers])
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

(def get-start
  (helpers/make-get :start))

(defn if-then-else
  [if-function then-function else]
  (if (if-function else)
    (then-function else)
    else))

(helpers/defcurried set-start
                    [e a network]
                    (if-then-else (comp maybe/nothing?
                                        (get-start e))
                                  (partial s/setval* [:start (:id e)] a)
                                  network))

(defn make-set-start-value
  [e a]
  (comp (helpers/set-value e a)
        (set-start e a)))

#?(:clj (defmacro event*
          [event-name & fs]
          `(helpers/get-entity ~event-name
                               Event.
                               ~@fs
                               (make-set-start-value ~event-name
                                                     helpers/nothing))))

(defn event
  []
  (event* e))
