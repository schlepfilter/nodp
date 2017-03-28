(ns nodp.helpers.primitives.event
  (:require [cats.core :as m]
            [cats.monad.maybe :as maybe]
            [cats.protocols :as p]
            [cats.util :as util]
            [#?(:clj  clojure.core.async
                :cljs cljs.core.async) :as async]
            [com.rpl.specter :as s]
            [loom.alg :as alg]
            [loom.graph :as graph]
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

(def call-functions
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

(defn reachable-subgraph
  [g n]
  (graph/subgraph g
                  (alg/bf-traverse g
                                   n)))

(defn get-modifiers
  [k entity network]
  (->> entity
       :id
       (reachable-subgraph (k (:dependency network)))
       alg/topsort
       (mapcat (:modifier network))))

;This definition is harder to read.
;(defn get-modifiers
;  [k entity network]
;  (mapcat
;    (:modifier network)
;    ((helpers/build filter
;                    (comp set
;                          (partial (helpers/flip alg/bf-traverse) (:id entity)))
;                    alg/topsort)
;      (k (:dependency network)))))

(defn modify-event!
  [occurrence e network]
  (call-functions
    (concat [(partial s/setval* [:time :event] (tuple/fst occurrence))
             (make-set-earliest-latest (maybe/just occurrence) e)]
            (get-modifiers :event e network))
    network))

(defn modify-network!
  [occurrence t e network]
  ;TODO modify behavior
  (call-functions [(partial modify-event! occurrence e)]
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

(defn queue
  [f]
  (async/put! (get-input) f))

(declare context)

(defrecord Event
  [id]
  p/Contextual
  (-get-context [_]
    context
    ;If context is inlined, the following error seems to occur.
    ;java.lang.LinkageError: loader (instance of clojure/lang/DynamicClassLoader): attempted duplicate class definition for name: "nodp/helpers/primitives/event/Event"
    )
  IFn
  (#?(:clj  invoke
      :cljs -invoke) [e a]
    ;e stands for an event and a stands for any as in Push-Pull Functional Reactive Programming.
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

#?(:clj (defmacro event*
          [event-name & fs]
          `(helpers/get-entity ~event-name
                               Event.
                               ~@fs
                               (make-set-earliest-latest helpers/nothing
                                                         ~event-name))))

(def context
  (helpers/reify-monad
    (fn [a]
      (event* e
              (make-set-earliest-latest
                (maybe/just (tuple/tuple (time/time 0) a))
                e)))
    (fn [ma f]
      (event*
        child-event
        (helpers/make-set-modifier
          (fn [network]
            network)
          child-event)
        (helpers/make-add-edges ma child-event)))))

(util/make-printable Event)

(defn event
  []
  (event* e))

(def activate
  (partial swap! helpers/network-state (partial s/setval* :active true)))
