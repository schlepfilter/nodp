(ns nodp.test.helpers.primitives.event
  (:require [cats.monad.maybe :as maybe]
            [clojure.test.check]
            [clojure.test.check.clojure-test
             :as clojure-test
             :include-macros true]
            [clojure.test.check.generators :as gen]
            [#?(:clj  clojure.core.async
                :cljs cljs.core.async) :as async]
            [#?(:clj  clojure.test
                :cljs cljs.test) :as test :include-macros true]
            [nodp.helpers :as helpers]
            [nodp.helpers.frp :as frp]
            [nodp.helpers.time :as time]
            [nodp.helpers.primitives.event :as event]
            [nodp.helpers.tuple :as tuple]
            [nodp.helpers.unit :as unit]
            [nodp.test.helpers :as test-helpers :include-macros true]))

(test/use-fixtures :each test-helpers/fixture)

(clojure-test/defspec
  call-inactive
  test-helpers/num-tests
  (test-helpers/restart-for-all [as (gen/vector test-helpers/any-equal)]
                                (let [e (frp/event)]
                                  (run! e as)
                                  (maybe/nothing? @e))))

(clojure-test/defspec
  call-active
  test-helpers/num-tests
  (test-helpers/restart-for-all [as (gen/vector test-helpers/any-equal)]
                                (let [e (frp/event)]
                                  (frp/activate)
                                  (run! e as)
                                  (= (tuple/snd @@e) (last as)))))

(clojure-test/defspec
  event-return
  test-helpers/num-tests
  (test-helpers/restart-for-all [a test-helpers/any-equal]
                                (= @@(-> (frp/event)
                                         helpers/infer
                                         (nodp.helpers/return a))
                                   (-> 0
                                       time/time
                                       (tuple/tuple a)))))

(def event->>=
  ;TODO refactor
  (gen/let [probabilities (gen/not-empty (gen/vector test-helpers/probability))
            input-events (gen/return (test-helpers/get-events probabilities))
            fs (gen/vector (test-helpers/function test-helpers/any-equal)
                           (count input-events))
            ;TODO randomize the simultaneity of input-events and input-event
            input-event test-helpers/event
            f (test-helpers/function gen/uuid)
            input-event-anys (gen/vector gen/uuid
                                         ((if (maybe/just? @input-event)
                                            dec
                                            identity)
                                           (count input-events)))
            input-events-anys (gen/vector test-helpers/any-equal
                                          (count input-events))
            calls (gen/shuffle (concat (map (fn [a]
                                              (fn []
                                                (input-event a)))
                                            input-event-anys)
                                       (map (fn [a input-event*]
                                              (fn []
                                                (input-event* a)))
                                            input-events-anys
                                            input-events)))]
           (gen/tuple (gen/return (helpers/<$> f input-event))
                      (gen/return (doall (map nodp.helpers/<$>
                                              fs
                                              input-events)))
                      (gen/return (partial doall (map helpers/funcall
                                                      (drop-last calls))))
                      (gen/return (last calls)))))

(defn get-earliest
  [e]
  (event/get-earliest e @helpers/network-state))

(defn all-nothing?
  [e es]
  (and (maybe/nothing? @e)
       (not-any? maybe/just? (map deref es))))

(def get-tuples
  (comp maybe/cat-maybes
        (partial map deref)))

(defn left-biased?*
  [e es]
  (->> es
       get-tuples
       (filter (comp (partial = (tuple/fst @@e))
                     tuple/fst))
       first
       tuple/snd
       (= (tuple/snd @@e))))

(def left-biased?
  (helpers/build or
                 all-nothing?
                 left-biased?*))

(defn right-most-earliest?
  [bound-event inner-events]
  (= (tuple/snd @(get-earliest (last inner-events)))
     (tuple/snd @@bound-event)))

(clojure-test/defspec
  event->>=-identity
  test-helpers/num-tests
  (test-helpers/restart-for-all
    [[outer-event inner-events calls call] event->>=]
    (let [bound-event (helpers/>>= outer-event
                                   (test-helpers/make-iterate inner-events))]
      (frp/activate)
      (calls)
      (let [outer-latest @outer-event
            inner-latests (doall (map deref inner-events))
            bound-latest @bound-event]
        (call)
        (if (= (map deref inner-events) inner-latests)
          (if (and (not= @outer-event outer-latest)
                   (maybe/just? @(last inner-events)))
            (right-most-earliest? bound-event inner-events)
            (= @bound-event bound-latest))
          (if (and (not= @outer-event outer-latest)
                   (= (map deref (drop-last inner-events))
                      (drop-last inner-latests)))
            (right-most-earliest? bound-event inner-events)
            (left-biased? bound-event inner-events)))))))

(def <>
  ;TODO refactor
  (gen/let [probabilities (gen/vector test-helpers/probability 2)
            [input-events fmapped-events]
            (test-helpers/events-tuple probabilities)
            ns (gen/vector (gen/sized (partial gen/choose 0))
                           (count input-events))
            calls (gen/shuffle (mapcat (fn [n e]
                                         (repeat n
                                                 (fn []
                                                   (e unit/unit))))
                                       ns
                                       input-events))]
           (gen/tuple (gen/return (partial run!
                                           helpers/funcall
                                           calls))
                      (gen/return (apply helpers/<> fmapped-events))
                      (gen/return fmapped-events))))

(clojure-test/defspec
  event-<>
  test-helpers/num-tests
  (test-helpers/restart-for-all [[call mappended-event fmapped-events] <>]
                                (frp/activate)
                                (call)
                                (left-biased? mappended-event fmapped-events)))

(clojure-test/defspec
  event-mempty
  test-helpers/num-tests
  (test-helpers/restart-for-all [a test-helpers/any-equal]
                                (= @(-> (frp/event)
                                        helpers/infer
                                        nodp.helpers/mempty)
                                   helpers/nothing)))

(defn get-generators
  [generator xforms**]
  (map (partial (helpers/flip gen/fmap) generator) xforms**))

(def any-nilable-equal
  (gen/one-of [test-helpers/any-equal (gen/return nil)]))

(def xform*
  (gen/one-of
    (concat [(gen/return (distinct))
             (gen/return (dedupe))
             (gen/fmap replace (gen/map test-helpers/any-equal
                                        test-helpers/any-equal))]
            (get-generators (test-helpers/function gen/boolean)
                            [drop-while filter remove take-while])
            (get-generators gen/s-pos-int [take-nth partition-all])
            (get-generators gen/int [drop take])
            (get-generators (test-helpers/function any-nilable-equal)
                            [keep keep-indexed])
            (get-generators (test-helpers/function test-helpers/any-equal)
                            [map map-indexed partition-by]))))

(def xform
  (->> xform*
       gen/vector
       gen/not-empty
       (gen/fmap (partial apply comp))))

(clojure-test/defspec
  transduce-identity
  test-helpers/num-tests
  ;TODO refactor
  (test-helpers/restart-for-all
    [input-event test-helpers/event
     xf xform
     f (test-helpers/function test-helpers/any-equal)
     init test-helpers/any-equal
     as (gen/vector test-helpers/any-equal)]
    (let [transduced-event (frp/transduce xf f init input-event)
          earliest @input-event]
      (frp/activate)
      (run! input-event as)
      (->> (comp (partial (helpers/flip cons) as)
                 tuple/snd)
           (maybe/maybe as earliest)
           (maybe/map-maybe (partial (comp unreduced
                                           (xf (comp maybe/just
                                                     second
                                                     vector)))
                                     helpers/nothing))
           (reduce f init)
           (= (tuple/snd @@transduced-event))))))
