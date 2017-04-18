(ns nodp.helpers.primitives.behavior
  (:refer-clojure :exclude [time])
  (:require [cats.monad.maybe :as maybe]
            [cats.protocols :as p]
            [cats.util :as util]
            [loom.graph :as graph]
            [nodp.helpers.primitives.event :as event]
            [nodp.helpers.time :as time]
            [nodp.helpers.tuple :as tuple]
            [nodp.helpers.unit :as unit]
            [nodp.helpers :as helpers])
  #?(:clj
           (:import [clojure.lang IDeref])
     :cljs (:require-macros [nodp.helpers.primitives.behavior
                             :refer
                             [behavior*]])))

(declare context)

(defrecord Behavior
  [id]
  p/Contextual
  (-get-context [_]
    context)
  IDeref
  (#?(:clj  deref
      :cljs -deref) [b]
    ;b stands for a behavior as in Push-Pull Functional Reactive Programming.
    (helpers/get-latest b @helpers/network-state))
  p/Printable
  (-repr [_]
    (str "#[behavior " id "]")))

#?(:clj (defmacro behavior*
          [event-name & fs]
          `(helpers/get-entity ~event-name Behavior. ~@fs)))

(util/make-printable Behavior)

(def context
  (helpers/reify-monad
    (fn [a]
      (behavior* _
                 (helpers/set-latest a)))
    (fn [ma f]
      (behavior*
        child-behavior
        (helpers/set-modifier
          (fn [network]
            (do (reset! helpers/network-state network)
                (let [parent-behavior (->> network
                                           (helpers/get-latest ma)
                                           f)]
                  (helpers/effect-swap-entity! parent-behavior)
                  (helpers/set-latest
                    (helpers/get-latest parent-behavior @helpers/network-state)
                    child-behavior
                    @helpers/network-state)))))
        (helpers/add-edge ma)))))

(declare time)

(defn start
  []
  (reset! helpers/network-state {:active      false
                                 :dependency  {:event    (graph/digraph)
                                               :behavior (graph/digraph)}
                                 :input-state (helpers/get-queue helpers/funcall)
                                 :id          0
                                 :modifier    {}
                                 :time        {:event    (time/time 0)
                                               :behavior (time/time 0)}})
  (def time
    (behavior* b
               (helpers/set-modifier
                 (fn [network]
                   (helpers/set-latest
                     (:behavior (:time network))
                     b
                     network))))))

(def restart
  ;TODO call stop
  start)

(defn switcher
  [parent-behavior parent-event]
  (let [child-behavior (behavior*
                         child-behavior*
                         (helpers/add-edge parent-behavior)
                         (helpers/set-latest @parent-behavior)
                         (helpers/set-modifier
                           (fn [network]
                             (helpers/set-latest
                               (helpers/get-latest
                                 (maybe/maybe parent-behavior
                                              (helpers/get-latest parent-event
                                                                  network)
                                              tuple/snd)
                                 network)
                               child-behavior*
                               network))))]
    (event/event* _
                  (helpers/add-edge parent-event)
                  (helpers/set-modifier
                    (fn [network]
                      (maybe/maybe network
                                   (helpers/get-latest parent-event network)
                                   (fn [x]
                                     (-> x
                                         tuple/snd
                                         (helpers/add-edge child-behavior
                                                           network)))))))
    child-behavior))

(def get-time
  (comp tuple/fst
        helpers/get-latest))

(defn calculus
  [f t current-behavior]
  ;TODO refactor
  ;TODO allow current-behavior's latest to be maybe
  (let [past-behavior
        (behavior* past-behavior*
                   (helpers/set-modifier
                     (fn [network]
                       (helpers/set-latest
                         (tuple/tuple (helpers/get-latest time network)
                                      (helpers/get-latest current-behavior
                                                          network))
                         past-behavior*
                         network)))
                   (helpers/set-latest (tuple/tuple (time/time 0)
                                                    helpers/nothing)))]
    (behavior* integration-behavior*
               (helpers/set-modifier
                 (fn [network]
                   ;TODO handle the case t is between past-behavior's time and current time
                   (cond
                     (maybe/maybe false
                                  t
                                  (comp (partial = @(helpers/get-latest time network))
                                        deref))
                     (helpers/set-latest
                       (maybe/just 0)
                       integration-behavior*
                       network)
                     (< @(get-time past-behavior network)
                        @(helpers/get-latest time network))
                     (helpers/set-latest
                       (f (helpers/get-latest current-behavior network)
                          (tuple/snd (helpers/get-latest past-behavior network))
                          (helpers/get-latest time network)
                          (get-time past-behavior network)
                          (maybe/maybe unit/unit
                                       t
                                       identity)
                          (helpers/get-latest integration-behavior* network))
                       integration-behavior*
                       network)
                     :else network)))
               (helpers/set-latest helpers/nothing)
               (helpers/add-edge current-behavior)
               (helpers/curry 2 (fn [integration-behavior** network]
                                  (helpers/add-edge integration-behavior**
                                                    past-behavior
                                                    network))))))
