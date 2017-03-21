(ns nodp.helpers.primitives.event
  (:require [cats.protocols :as p]
            [cats.util :as util]
            [nodp.helpers :as helpers])
  #?(:clj
     (:import (clojure.lang IDeref))))

(defn- deref*
  [e]
  (helpers/get-value e @helpers/network-state))

(defrecord Event
  [id]
  p/Printable
  (-repr [_]
    (str "#[event " id "]"))
  #?@(:clj  [IDeref
             (deref [e]
               (deref* e))]
      :cljs [IDeref
             (-deref [e]
                     (deref* e))]))

(util/make-printable Event)
