(ns nodp.helpers.primitives.event
  (:require [cats.protocols :as p]
            [cats.util :as util]
            [nodp.helpers :as helpers])
  #?(:clj
     (:import (clojure.lang IDeref))))

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
