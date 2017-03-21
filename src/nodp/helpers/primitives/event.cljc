(ns nodp.helpers.primitives.event
  (:require [cats.protocols :as p]
            [cats.util :as util]
            [nodp.helpers :as helpers])
  #?(:clj
           (:import (clojure.lang IDeref))
     :cljs (:require-macros [nodp.helpers.primitives.event :refer [defevent defevents]])))

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

       (defmacro defevents
         [clj]
         `(do (defevent {:invokable true
                         :clj       ~clj})
              (defevent {:invokable false
                         :clj       ~clj})))))

(defevents #?(:clj  true
              :cljs false))
