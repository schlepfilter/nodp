(ns nodp.helpers.primitives.event
  (:require [cats.protocols :as p]
            [cats.util :as util])
  #?(:cljs (:require-macros [nodp.helpers.primitives.event :refer [defevent]])))

(defn get-record-name
  [invokable]
  (if invokable
    'Event
    'MemptyEvent))

#?(:clj (defmacro defevent
          [invokable]
          `(do (defrecord ~(get-record-name invokable)
                 [~'id]
                 p/Printable
                 (~'-repr [_#]
                   (str "#[event " (pr-str ~'id) "]")))

               (util/make-printable ~(get-record-name invokable)))))

(defevent true)

(defevent false)
