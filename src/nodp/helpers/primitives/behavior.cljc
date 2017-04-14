(ns nodp.helpers.primitives.behavior
  (:refer-clojure :exclude [time])
  (:require [cats.monad.maybe :as maybe]
            [cats.protocols :as p]
            [cats.util :as util]
            [com.rpl.specter :as s]
            [loom.graph :as graph]
            [nodp.helpers.primitives.event :as event]
            [nodp.helpers.time :as time]
            [nodp.helpers.tuple :as tuple]
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
                  (helpers/set-latest
                    (helpers/get-latest parent-behavior @helpers/network-state)
                    child-behavior
                    @helpers/network-state)))))
        (helpers/add-edge ma)))))

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

(declare time)

(defn start
  []
  (reset! helpers/network-state {:active      false
                                 :dependency  {:event    (graph/digraph)
                                               :behavior (graph/digraph)}
                                 :input-state (helpers/get-queue helpers/funcall)
                                 :id          0
                                 :modifier    {}
                                 :time        {:event (time/time 0)}})
  (alter-var-root #'time
                  (fn [_]
                    (behavior* _))))

(def restart
  ;TODO call stop
  start)

(def activate
  (juxt (partial swap! helpers/network-state (partial s/setval* :active true))
        time/start
        (fn []
          ;switcher's behavior-valued event may be a unit event
          (reset! helpers/network-state (event/modify-behavior! (time/now) @helpers/network-state)))))
