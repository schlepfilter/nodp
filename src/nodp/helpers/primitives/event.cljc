(ns nodp.helpers.primitives.event
  (:require [cats.protocols :as p]
            [cats.util :as util]
            [nodp.helpers :as helpers]
            [cats.monad.maybe :as maybe])
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

#?(:clj (defmacro event*
          [node-name & fs]
          `(helpers/get-node ~node-name
                             Event.
                             ~@fs
                             ;TODO set start and value
                             )))

(defn event
  []
  (event* e))
