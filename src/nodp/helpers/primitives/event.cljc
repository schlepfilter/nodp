(ns nodp.helpers.primitives.event
  (:require [cats.monad.maybe :as maybe]
            [cats.protocols :as p]
            [cats.util :as util]
            [#?(:clj  clojure.core.async
                :cljs cljs.core.async) :as async]
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
    (if (= past current)
      (get-new-time past)
      current)))

(defn get-times
  []
  (let [past (time/now)]
    [past (get-new-time past)]))

(def call-functions!
  (helpers/flip (partial reduce (helpers/flip helpers/funcall))))

(def get-earliest
  (helpers/make-get :earliest))

(defn if-then-else
  [if-function then-function else]
  (if (if-function else)
    (then-function else)
    else))

(helpers/defcurried set-earliest
                    [a e network]
                    (if-then-else (comp maybe/nothing?
                                        (get-earliest e))
                                  (partial s/setval* [:earliest (:id e)] a)
                                  network))

(defn make-set-earliest-latest
  [a e]
  (comp (helpers/set-latest a e)
        (set-earliest a e)))

(defn modify-event!
  [occurrence e network]
  (call-functions!
    ;TODO concatenate modifiers
    (concat [(partial s/setval* [:time :event] (tuple/fst occurrence))
             (make-set-earliest-latest (maybe/just occurrence) e)])
    network))

(defn modify-network!
  [occurrence t e network]
  ;TODO modify behavior
  (call-functions! [(partial modify-event! occurrence e)]
                   network))
(def run-effects!
  (helpers/build run!
                 (helpers/curry 2 (helpers/flip helpers/funcall))
                 :effects))

(defn make-handle
  [a e]
  (fn []
    (let [[past current] (get-times)]
      (->> @helpers/network-state
           (modify-network! (tuple/tuple past a) current e)
           (reset! helpers/network-state))
      (run-effects! @helpers/network-state))))

(defn get-input
  []
  (:input @helpers/network-state))

(def queue
  (partial async/put! get-input))

(defrecord Event
  [id]
  IFn
  (#?(:clj  invoke
      :cljs -invoke) [e a]
    ;e stands for an event as in Push-Pull Functional Reactive Programming.
    (if (:active @helpers/network-state)
      (-> (make-handle a e)
          queue)))
  IDeref
  (#?(:clj  deref
      :cljs -deref) [e]
    (helpers/get-latest e @helpers/network-state))
  p/Printable
  (-repr [_]
    (str "#[event " id "]")))

(util/make-printable Event)

#?(:clj (defmacro event*
          [event-name & fs]
          `(helpers/get-entity ~event-name
                               Event.
                               ~@fs
                               (make-set-earliest-latest helpers/nothing
                                                         ~event-name))))

(defn event
  []
  (event* e))

(def activate
  (partial swap! helpers/network-state (partial s/setval* :active true)))
