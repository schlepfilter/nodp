(ns nodp.helpers.primitives.event
  (:require [cats.protocols :as p]
            [cats.util :as util]
            [nodp.helpers :as helpers])
  #?(:cljs (:require-macros [nodp.helpers.primitives.event :refer [defevent]]))
  #?(:clj
     (:import (clojure.lang IDeref))))

(defn get-record-name
  [invokable]
  (if invokable
    'Event
    'MemptyEvent))

#?(:clj
   (do (defmacro defevent
         [{:keys [invokable clj]}]
         `(do (defrecord ~(get-record-name invokable)
                [~'id]
                ~@(if clj
                    `[IDeref
                      (deref [e#]
                             (helpers/get-value e# @helpers/network-state))])
                p/Printable
                (~'-repr [_#]
                  (str "#[event " (pr-str ~'id) "]")))

              (util/make-printable ~(get-record-name invokable))))

       (defevent {:invokable true
                  :clj       true})

       (defevent {:invokable false
                  :clj       true})))

#?(:cljs
   (do (defevent {:invokable true
                  :clj       false})

       (defevent {:invokable false
                  :clj       false})))

