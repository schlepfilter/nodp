(ns nodp.helpers.primitives.behavior
  (:refer-clojure :exclude [time])
  (:require [cats.monad.maybe :as maybe]
            [cats.protocols :as protocols]
            [cats.util :as util]
            [#?(:clj  clojure.core.async
                :cljs cljs.core.async) :as async]
            [com.rpl.specter :as s]
            [cuerdas.core :as cuerdas]
    #?@(:clj [
            [chime :as chime]
            [clj-time.core :as t]
            [clj-time.periodic :as p]])
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
  protocols/Contextual
  (-get-context [_]
    context)
  IDeref
  (#?(:clj  deref
      :cljs -deref) [b]
    ;b stands for a behavior as in Push-Pull Functional Reactive Programming.
    (helpers/get-latest b @helpers/network-state))
  protocols/Printable
  (-repr [_]
    (str "#[behavior " id "]")))

#?(:clj (defmacro behavior*
          [behavior-name & fs]
          `(helpers/get-entity ~behavior-name Behavior. ~@fs)))

(util/make-printable Behavior)

(def context
  (helpers/reify-monad
    (fn [a]
      (behavior* _
                 (helpers/set-latest a)))
    (fn [ma f]
      (behavior*
        child-behavior
        (helpers/append-modify
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

#?(:clj (defn get-periods
          [rate]
          (rest (p/periodic-seq (t/now) (t/millis rate)))))

(defn handle
  [_]
  (if (:active @helpers/network-state)
    (event/queue
      (fn []
        (reset! helpers/network-state
                (event/modify-behavior! (time/now) @helpers/network-state))
        ;TODO add a test that fails without run-effects!
        (event/run-effects! @helpers/network-state)))))

(defn start
  ([]
   (start #?(:clj  Double/POSITIVE_INFINITY
             :cljs js/Number.POSITIVE_INFINITY)))
  ([rate]
    ;TODO add a test that tests rate
   (reset! helpers/network-state (helpers/get-initial-network))
   (swap! helpers/network-state
          (partial s/setval*
                   :cancel
                   (if (= rate #?(:clj  Double/POSITIVE_INFINITY
                                  :cljs js/Number.POSITIVE_INFINITY))
                     helpers/nop
                     #?(:clj  (chime/chime-at (get-periods rate) handle)
                        :cljs (->> (js/setInterval handle rate)
                                   (partial js/clearInterval))))))
   (def time
     (behavior* b
                (helpers/append-modify
                  (fn [network]
                    (helpers/set-latest
                      (:time network)
                      b
                      network)))))
   (run! helpers/funcall (flatten ((juxt :defs
                                         :synchronizes)
                                    @helpers/registry)))))

(defn stop
  []
  ((:cancel @helpers/network-state))
  (async/close! (:input-state @helpers/network-state)))

(defn restart
  [& more]
  (stop)
  (apply start more))

(defn switcher
  [parent-behavior parent-event]
  (let [child-behavior (behavior*
                         child-behavior*
                         (helpers/add-edge parent-behavior)
                         (helpers/set-latest @parent-behavior)
                         (helpers/append-modify
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
                  (helpers/append-modify
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
  [f lower-limit-maybe current-behavior]
  ;TODO refactor
  (let [past-behavior
        (behavior* past-behavior*
                   (helpers/append-modify
                     (fn [network]
                       (helpers/set-latest
                         (tuple/tuple (:time network)
                                      (helpers/get-latest current-behavior
                                                          network))
                         past-behavior*
                         network)))
                   (helpers/set-latest (tuple/tuple (time/time 0)
                                                    helpers/nothing)))]
    (behavior*
      integration-behavior*
      (helpers/append-modify
        (fn [network]
          (cond (and (maybe/maybe true
                                  lower-limit-maybe
                                  (comp (partial >
                                                 @(get-time past-behavior
                                                            network))
                                        deref))
                     (< @(get-time past-behavior network)
                        @(:time network)))
                (helpers/set-latest
                  (f (helpers/get-latest current-behavior network)
                     (tuple/snd (helpers/get-latest past-behavior network))
                     (:time network)
                     (get-time past-behavior network)
                     (helpers/get-latest integration-behavior* network))
                  integration-behavior*
                  network)
                (maybe/nothing? lower-limit-maybe) network
                (= @(:time network) @@lower-limit-maybe)
                (helpers/set-latest
                  (maybe/just 0)
                  integration-behavior*
                  network)
                (and (< @@lower-limit-maybe @(:time network))
                     (< @(get-time past-behavior network)
                        @(:time network)))
                (helpers/set-latest
                  (f (helpers/get-latest current-behavior network)
                     (+ (tuple/snd (helpers/get-latest past-behavior network))
                        (/ (* (- (helpers/get-latest current-behavior network)
                                 (tuple/snd (helpers/get-latest past-behavior
                                                                network)))
                              (- @@lower-limit-maybe @(get-time past-behavior
                                                                network)))
                           (- @(:time network)
                              @(get-time past-behavior network))))
                     (:time network)
                     @lower-limit-maybe
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

#?(:clj
   (do (defmacro make-get-defs
         [symbols]
         (mapv (fn [x]
                 `(fn []
                    (def ~x
                      (behavior* _#))))
               symbols))

       (defmacro make-get-synchronizes
         [javascript-namespace symbols]
         (mapv (fn [x]
                 `(fn [network#]
                    (helpers/set-latest
                      ~(->> x
                            cuerdas/camel
                            (str "js/" javascript-namespace ".")
                            symbol)
                      ~x
                      network#)))
               symbols))

       (defmacro set-registry!
         [javascript-namespace & symbols]
         `(swap! helpers/registry
                 ;TODO refactor
                 (comp (partial s/setval*
                                [:synchronizes s/END]
                                (make-get-synchronizes ~javascript-namespace
                                                       ~symbols))
                       (partial s/setval*
                                [:defs s/END]
                                (make-get-defs ~symbols)))))))
