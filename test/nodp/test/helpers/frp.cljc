(ns nodp.test.helpers.frp
  (:require [clojure.test.check]
            [clojure.test.check.clojure-test
             :as clojure-test
             :include-macros true]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop :include-macros true]
            [cats.core :as m]
            [cats.monad.maybe :as maybe]
            [cats.protocols :as p]
            [#?(:clj  clojure.core.async
                :cljs cljs.core.async) :as async]
            [#?(:clj  clojure.test
                :cljs cljs.test) :as test :include-macros true]
            [nodp.helpers :as helpers]
            [nodp.helpers.frp :as frp]
            [nodp.helpers.primitives.event :as event]
            [nodp.helpers.time :as time]
            [nodp.helpers.tuple :as tuple]
            [nodp.helpers.unit :as unit]
            [nodp.test.helpers :as test-helpers]
    #?(:clj
            [riddley.walk :as walk]))
  #?(:cljs (:require-macros [nodp.test.helpers.frp :refer [with-exit
                                                           with-exitv]])))

(defn fixture
  [f]
  (frp/restart)
  (with-redefs [event/queue helpers/funcall]
    (f)))

(test/use-fixtures :each fixture)

(clojure-test/defspec
  invoke-inactive
  10
  (prop/for-all [as (gen/vector gen/any)]
                (let [e (frp/event)]
                  (run! e as)
                  (maybe/nothing? @e))))

(clojure-test/defspec
  invoke-active
  10
  (prop/for-all [as (gen/vector gen/any)]
                (let [e (frp/event)]
                  (frp/activate)
                  (run! e as)
                  (= (tuple/snd @@e) (last as)))))

(def probability
  (gen/double* {:min 0 :max 1}))

(clojure-test/defspec
  event-return
  10
  (prop/for-all [a gen/any]
                (= @@(m/return (helpers/infer (frp/event)) a)
                   (tuple/tuple (time/time 0) a))))

#?(:clj (defmacro with-exit
          [exit-name & body]
          (potemkin/unify-gensyms
            `(let [exit-state## (atom helpers/nothing)]
               ~(walk/walk-exprs
                  (partial = exit-name)
                  (fn [_#]
                    `(comp (partial reset! exit-state##)
                           maybe/just))
                  (cons `do body))
               @@exit-state##))))

(clojure-test/defspec
  with-exit-identity
  10
  (prop/for-all [a gen/any
                 b gen/any]
                (= (with-exit exit
                              (exit a)
                              b)
                   a)))

#?(:clj (defmacro with-exitv
          [exit-name & body]
          (potemkin/unify-gensyms
            `(let [exits-state## (atom [])]
               ~(walk/walk-exprs
                  (partial = exit-name)
                  (fn [_#]
                    `(comp (partial swap! exits-state##)
                           (helpers/curry 2 (helpers/flip conj))))
                  (cons `do body))
               @exits-state##))))

(clojure-test/defspec
  with-exitv-identity
  10
  (prop/for-all [as (gen/vector gen/any)
                 b gen/any]
                (= (with-exitv exit
                               (->> as
                                    (map exit)
                                    doall)
                               b)
                   as)))

(clojure-test/defspec
  on-identity
  10
  (prop/for-all [as (gen/vector gen/any)]
                (= (with-exitv exit
                               (let [e (frp/event)]
                                 (frp/on exit e)
                                 (frp/activate)
                                 (run! e as)))
                   as)))

(defn conj-event
  [coll probability*]
  (->> coll
       count
       inc
       (* probability*)
       int
       (if (= 1.0 probability*)
         0)
       (nth (conj coll (frp/event)))
       (conj coll)))

(def get-events
  (partial reduce conj-event []))

(def events
  (->> probability
       gen/vector
       gen/not-empty
       (gen/fmap get-events)))

(def events-tuple
  (gen/bind events
            (fn [es]
              (->> (fn [fs]
                     (gen/return
                       (map (fn [f e]
                              ((m/lift-a 1 f) e))
                            fs
                            es)))
                   (gen/bind (gen/vector test-helpers/function (count es)))
                   (gen/tuple (gen/return es))))))

(def events-behaviors
  (gen/bind events-tuple
            (fn [[input-events fmapped-events]]
              (gen/tuple (gen/return input-events)
                         (gen/bind (gen/vector gen/any (count input-events))
                                   (fn [as]
                                     (gen/return
                                       (map frp/stepper
                                            as
                                            fmapped-events))))))))

(defn make-iterate
  [coll]
  (let [state (atom coll)]
    (fn [& _]
      (let [result (first @state)]
        (swap! state rest)
        result))))

(defn contains-value?
  [coll x]
  (-> coll
      set
      (contains? x)))

(def call-units
  (partial run! (partial (helpers/flip helpers/funcall) unit/unit)))

(clojure-test/defspec
  event->>=-nonmember
  5
  (prop/for-all [[input-events fmapped-events] events-tuple]
                (let [outer-event (frp/event)
                      bound-event (->> fmapped-events
                                       make-iterate
                                       (m/>>= outer-event))]
                  (frp/activate)
                  (dotimes [_ (-> input-events
                                  count
                                  dec)]
                    (outer-event unit/unit))
                  (call-units input-events)
                  (or (maybe/nothing? @bound-event)
                      (contains-value? (->> fmapped-events
                                            drop-last
                                            (map deref))
                                       @bound-event)))))

(def get-time
  (comp tuple/fst
        deref
        deref))

(clojure-test/defspec
  event->>=-delay
  5
  (prop/for-all [inner-events events]
                (let [outer-event (frp/event)
                      bound-event (->> inner-events
                                       make-iterate
                                       (m/>>= outer-event))]
                  (frp/activate)
                  (dotimes [_ (-> inner-events
                                  count
                                  dec)]
                    (outer-event unit/unit))
                  (call-units inner-events)
                  (outer-event unit/unit)
                  (= (get-time outer-event) (get-time bound-event)))))

(clojure-test/defspec
  event->>=-member
  5
  (prop/for-all [[inner-events fmapped-events] events-tuple]
                (let [outer-event (frp/event)
                      bound-event (->> fmapped-events
                                       make-iterate
                                       (m/>>= outer-event))]
                  (frp/activate)
                  (dotimes [_ (count inner-events)]
                    (outer-event unit/unit))
                  (call-units inner-events)
                  (contains-value? (map deref fmapped-events)
                                   @bound-event))))

(clojure-test/defspec
  event->>=-left-bias
  5
  (prop/for-all [[inner-events fmapped-events] events-tuple]
                (let [outer-event (frp/event)
                      bound-event (->> fmapped-events
                                       make-iterate
                                       (m/>>= outer-event))]
                  (frp/activate)
                  (dotimes [_ (count inner-events)]
                    (outer-event unit/unit))
                  (call-units inner-events)
                  (->> fmapped-events
                       (map deref)
                       (filter (comp (partial = (get-time bound-event))
                                     tuple/fst
                                     deref))
                       first
                       (= @bound-event)))))

(clojure-test/defspec
  behavior-return
  10
  (prop/for-all [a gen/any]
                (= @(m/return (helpers/infer (frp/behavior unit/unit)) a)
                   a)))
