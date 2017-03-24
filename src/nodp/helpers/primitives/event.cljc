(ns nodp.helpers.primitives.event
  (:require [cats.monad.maybe :as maybe]
            [cats.protocols :as p]
            [cats.util :as util]
            [com.rpl.specter :as s]
            [nodp.helpers :as helpers]
            [nodp.helpers.time :as time]
            [nodp.helpers.tuple :as tuple])
  #?(:clj
           (:import (clojure.lang IDeref IFn))
     :cljs (:require-macros [nodp.helpers.primitives.event :refer [event*]])))

(defn get-new-time
  [past]
  (let [current (time/now)]
    (if-not (= past current)
      current
      (get-new-time past))))

(defn get-times
  []
  (let [past (time/now)]
    [past (get-new-time past)]))

(defn funcall
  [f & more]
  (apply f more))

(def call-functions!
  (helpers/flip (partial reduce (helpers/flip funcall))))

(def get-origin
  (helpers/make-get :origin))

(defn if-then-else
  [if-function then-function else]
  (if (if-function else)
    (then-function else)
    else))

(helpers/defcurried set-origin
                    [a e network]
                    (if-then-else (comp maybe/nothing?
                                        (get-origin e))
                                  (partial s/setval* [:origin (:id e)] a)
                                  network))

(defn make-set-origin-value
  [a e]
  (comp (helpers/set-value a e)
        (set-origin a e)))

(defn make-modify-event!
  [occurrence e]
  (partial call-functions!
           (concat [(partial s/setval* [:time :event] (tuple/fst occurrence))
                    (make-set-origin-value (maybe/just occurrence) e)])))

(defn modify-network!
  [occurrence t e network]
  (call-functions! [(make-modify-event! occurrence e)]
                   network))

(defrecord Event
  [id]
  IFn
  (#?(:clj  invoke
      :cljs -invoke) [e a]
    ;e stands for an event as in Push-Pull Functional Reactive Programming.
    (if (:active @helpers/network-state)
      (let [[past current] (get-times)]
        (reset! helpers/network-state
                (modify-network! (tuple/tuple past a)
                                 current
                                 e
                                 @helpers/network-state)))))
  IDeref
  (#?(:clj  deref
      :cljs -deref) [e]
    (helpers/get-value e @helpers/network-state))
  p/Printable
  (-repr [_]
    (str "#[event " id "]")))

(util/make-printable Event)

#?(:clj (defmacro event*
          [event-name & fs]
          `(helpers/get-entity ~event-name
                               Event.
                               ~@fs
                               (make-set-origin-value helpers/nothing
                                                      ~event-name))))

(defn event
  []
  (event* e))

(def activate
  (partial swap! helpers/network-state (partial s/setval* :active true)))
