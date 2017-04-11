(ns nodp.helpers.primitives.event
  (:refer-clojure :exclude [transduce])
  (:require [cats.builtin]
            [cats.core :as m]
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

(def get-earliest
  (helpers/make-get :earliest))

(defn if-then-else
  [if-function then-function else]
  ((helpers/build if
                  if-function
                  then-function
                  identity)
    else))

(helpers/defcurried set-earliest
                    [a e network]
                    (if-then-else (comp maybe/nothing?
                                        (get-earliest e))
                                  (partial s/setval* [:earliest (:id e)] a)
                                  network))

(helpers/defcurried set-earliest-latest
                    [a e network]
                    (->> network
                         (helpers/set-latest a e)
                         (set-earliest a e)))

(def call-functions
  (helpers/flip (partial reduce (helpers/flip helpers/funcall))))

(defn make-get-modifiers*
  [network]
  (comp (partial mapcat (:modifier network))
        alg/topsort))

(def get-behavior-modifiers
  (nodp.helpers/<*> make-get-modifiers*
                    (comp :behavior
                          :dependency)))

(defn modify-behavior!
  [t network]
  (call-functions (cons (partial s/setval* [:time :behavior] t)
                        (get-behavior-modifiers network))
                  network))

(defn reachable-subgraph
  [g n]
  (->> n
       (alg/bf-traverse g)
       (graph/subgraph g)))

(defn get-event-modifiers
  [e network]
  (->> e
       :id
       (reachable-subgraph (:event (:dependency network)))
       ((make-get-modifiers* network))))

(defn modify-event!
  [occurrence e network]
  (call-functions
    (concat [(partial s/setval* [:time :event] (tuple/fst occurrence))
             (set-earliest-latest (maybe/just occurrence) e)]
            (get-event-modifiers e network))
    network))

(defn modify-network!
  [occurrence t e network]
  (call-functions [(partial modify-behavior! (tuple/fst occurrence))
                   (partial modify-event! occurrence e)
                   (partial modify-behavior! t)]
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
  (:input-state @helpers/network-state))

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
  #?(:clj (applyTo [e coll]
            (-> coll
                first
                e)))
  IDeref
  (#?(:clj  deref
      :cljs -deref) [e]
    (helpers/get-latest e @helpers/network-state))
  p/Printable
  (-repr [_]
    (str "#[event " id "]")))

(helpers/defcurried call-modifier
                    [e network]
                    (call-functions ((:id e) (:modifier network))
                                    network))

#?(:clj (defmacro event*
          [event-name & fs]
          `(helpers/get-entity
             ~event-name
             Event.
             call-modifier
             ~@fs
             (set-earliest-latest helpers/nothing))))

(defn now?
  [e network]
  (maybe/maybe false
               (helpers/get-latest e network)
               (comp (partial = (:event (:time network)))
                     tuple/fst)))

(def get-value
  (comp tuple/snd
        deref
        helpers/get-latest))

(defn make-sync
  [parent-event child-event]
  (helpers/build helpers/set-latest
                 (partial helpers/get-latest parent-event)
                 (constantly child-event)
                 identity))

(defn make-merge-sync
  [parent-event child-event]
  (helpers/set-modifier
    (partial if-then-else
             (helpers/build and
                            (partial now? parent-event)
                            (partial (complement now?) child-event))
             (make-sync parent-event child-event))
    child-event))

(helpers/defcurried
  delay-sync->>=
  [parent-event child-event network]
  (maybe/maybe
    network
    (get-earliest parent-event network)
    (comp (fn [a]
            (set-earliest-latest a child-event network))
          maybe/just
          (partial nodp.helpers/<*> (tuple/tuple (:event (:time network)) identity)))))

(def get-time-value
  (comp deref
        tuple/fst
        deref
        helpers/get-latest))

(helpers/defcurried modify->>=!
                    [ma f child-event network]
                    ;Testing with now? assumes times for events are strictly increasing.
                    (if (now? ma network)
                      (do (reset! helpers/network-state network)
                          (let [parent-event (->> network
                                                  (get-value ma)
                                                  f)]
                            (call-functions ((juxt helpers/add-edge
                                                   make-merge-sync
                                                   delay-sync->>=)
                                              parent-event
                                              child-event)
                                            @helpers/network-state)))
                      network))

(helpers/defcurried modify-<>!
                    [left-event right-event child-event network]
                    (-> (cond (->> network
                                   (helpers/get-latest left-event)
                                   maybe/nothing?)
                              right-event
                              (->> network
                                   (helpers/get-latest right-event)
                                   maybe/nothing?)
                              left-event
                              (< (get-time-value left-event network)
                                 (get-time-value right-event network))
                              right-event
                              :else
                              left-event)
                        (helpers/get-latest network)
                        (set-earliest-latest child-event network)))

(def context
  (helpers/reify-monad
    (fn [a]
      (event* _
              (set-earliest-latest (maybe/just (tuple/tuple (time/time 0) a)))))
    (fn [ma f]
      (let [child-event (event* child-event*
                                (helpers/set-modifier
                                  (modify->>=! ma f child-event*))
                                (helpers/add-edge ma))]
        ;TODO call modify-events! in event*
        ;The second argument of swap! "may be called
        ;multiple times, and thus should be free of side effects" (clojure.core).
        ;The evaluation of the following code terminates in ClojureScript but doesn't seem to terminate in Clojure presumably because the second argument of swap! calls reset! on the same atom as swap!
        ;(event* child-event
        ;        (modify->>=! ma f))
        ;The evaluation of the following code terminates in ClojureScript but doesn't seem to terminate in Clojure
        ;(let [state (atom 0)]
        ;  (swap! state
        ;         (fn [_] (swap! state inc))))
        child-event))
    p/Semigroup
    (-mappend [_ left-event right-event]
              (event* child-event
                      (helpers/set-modifier
                        (modify-<>! left-event right-event child-event))
                      (helpers/add-edge left-event)
                      (helpers/add-edge right-event)))
    p/Monoid
    (-mempty [_]
             (event* _))))

(util/make-printable Event)

(helpers/defcurried modify-transduce-transduction-event
                    [step f parent-event transduction-event network]
                    (if (now? parent-event network)
                      (let [stepped (step helpers/nothing
                                          (tuple/snd
                                            @(helpers/get-latest parent-event
                                                                 network)))]
                        (maybe/maybe
                          network
                          stepped
                          (fn [stepped-value]
                            (helpers/set-latest
                              (maybe/just
                                (tuple/tuple
                                  (:event (:time network))
                                  (f (tuple/snd
                                       @(helpers/get-latest transduction-event
                                                            network))
                                     stepped-value)))
                              transduction-event
                              network))))
                      network))

(helpers/defcurried modify-transduce-child-event
                    [transduction-event child-event network]
                    (if-then-else (partial now? transduction-event)
                                  (make-sync transduction-event child-event)
                                  network))

(defn transduce
  [xform f init parent-event]
  (let [step (xform (comp maybe/just
                          second
                          vector))
        transduction-event
        (event*
          transduction-event*
          (set-earliest-latest (maybe/just (tuple/tuple (time/time 0) init)))
          (helpers/set-modifier
            (modify-transduce-transduction-event step
                                                 f
                                                 parent-event
                                                 transduction-event*))
          (helpers/add-edge parent-event))]
    (event* child-event
            (helpers/set-modifier
              (modify-transduce-child-event transduction-event child-event))
            (helpers/add-edge transduction-event))))

(defn start
  []
  (reset! helpers/network-state {:active      false
                                 :dependency  {:event    (graph/digraph)
                                               :behavior (graph/digraph)}
                                 :input-state (helpers/get-queue helpers/funcall)
                                 :id          0
                                 :modifier    {}
                                 :time        {:event (time/time 0)}}))

(def restart
  ;TODO call stop
  start)

(def activate
  (juxt (partial swap! helpers/network-state (partial s/setval* :active true))
        time/start
        (fn []
          ;switcher's behavior-valued event may be a unit event
          (reset! helpers/network-state (modify-behavior! (time/now) @helpers/network-state)))))

