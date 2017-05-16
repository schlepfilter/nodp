(ns nodp.test.helpers.primitives.event
  (:refer-clojure :exclude [transduce])
  (:require [#?(:clj  clojure.test
                :cljs cljs.test) :as test :include-macros true]
            [cats.monad.maybe :as maybe]
            [clojure.test.check]
            [clojure.test.check.clojure-test
             :as clojure-test
             :include-macros true]
            [clojure.test.check.generators :as gen]
            [nodp.helpers :as helpers]
            [nodp.helpers.frp :as frp]
            [nodp.helpers.primitives.event :as event]
            [nodp.helpers.time :as time]
            [nodp.helpers.tuple :as tuple]
            [nodp.helpers.unit :as unit]
            [nodp.test.helpers :as test-helpers :include-macros true]))

(test/use-fixtures :each test-helpers/fixture)

(clojure-test/defspec
  call-inactive
  test-helpers/cljc-num-tests
  (test-helpers/restart-for-all [as (gen/vector test-helpers/any-equal)]
                                (let [e (frp/event)]
                                  (run! e as)
                                  (= @e []))))

(clojure-test/defspec
  call-active
  test-helpers/cljc-num-tests
  (test-helpers/restart-for-all [as (gen/vector test-helpers/any-equal)]
                                (let [e (frp/event)]
                                  (frp/activate)
                                  (run! e as)
                                  (= (map tuple/snd @e) as))))

(clojure-test/defspec
  event-return
  test-helpers/cljc-num-tests
  (test-helpers/restart-for-all [a test-helpers/any-equal]
                                (= (last @(-> (frp/event)
                                              helpers/infer
                                              (nodp.helpers/return a)))
                                   (-> 0
                                       time/time
                                       (tuple/tuple a)))))

(def event->>=
  ;TODO refactor
  (gen/let [probabilities* (test-helpers/probabilities 3)
            [outer-input-event & inner-input-events :as input-events]
            (gen/return (test-helpers/get-events probabilities*))
            ;TODO generalize gen/uuid
            fs (gen/vector (test-helpers/function gen/uuid)
                           (count input-events))
            input-event-anys (gen/vector gen/uuid
                                         ((if (empty? @outer-input-event)
                                            identity
                                            dec)
                                           (dec (count input-events))))
            calls (gen/shuffle
                    (concat (map (helpers/curry 2 outer-input-event)
                                 input-event-anys)
                            (map (fn [inner-input-event as]
                                   #(if (not= inner-input-event
                                              outer-input-event)
                                     (run! inner-input-event as)))
                                 inner-input-events
                                 (gen/vector (gen/vector test-helpers/any-equal)
                                             (count inner-input-events)))))]
           (gen/tuple (gen/return (doall (map nodp.helpers/<$>
                                              fs
                                              input-events)))
                      (gen/return (partial doall (map helpers/funcall
                                                      calls))))))

(defn delay-inner-occs
  [outer-occ inner-occs]
  (event/delay-time-occs (tuple/fst outer-occ) inner-occs))

(def delay-inner-occs-coll
  (partial map delay-inner-occs))

(clojure-test/defspec
  event->>=-identity
  test-helpers/cljc-num-tests
  (test-helpers/restart-for-all
    [[[outer-event & inner-events] call] (gen/no-shrink event->>=)]
    (let [bound-event (helpers/>>= outer-event
                                   (test-helpers/make-iterate inner-events))]
      (frp/activate)
      (call)
      (->> inner-events
           (map deref)
           (delay-inner-occs-coll @outer-event)
           (reduce event/merge-occs [])
           (= @bound-event)))))

(def <>
  ;TODO refactor
  (gen/let [probabilities (gen/vector test-helpers/probability 2)
            [input-events fmapped-events]
            (test-helpers/events-tuple probabilities)
            ns (gen/vector (gen/sized (partial gen/choose 0))
                           (count input-events))
            calls (gen/shuffle (mapcat (fn [n e]
                                         (repeat n
                                                 (partial e unit/unit)))
                                       ns
                                       input-events))]
           (gen/tuple (gen/return fmapped-events)
                      (gen/return (apply helpers/<> fmapped-events))
                      (gen/return (partial run!
                                           helpers/funcall
                                           calls)))))

(clojure-test/defspec
  event-<>
  test-helpers/cljc-num-tests
  (test-helpers/restart-for-all [[fmapped-events mappended-event call] <>]
                                (frp/activate)
                                (call)
                                (->> fmapped-events
                                     (map deref)
                                     (apply event/merge-occs)
                                     (= @mappended-event))))

(defn get-generators
  [generator xforms**]
  (map (partial (helpers/flip gen/fmap) generator) xforms**))

(def any-nilable-equal
  (gen/one-of [test-helpers/any-equal (gen/return nil)]))

(def xform*
  (gen/one-of
    (concat (map (comp gen/return
                       helpers/funcall)
                 [dedupe distinct])
            (get-generators gen/s-pos-int [take-nth partition-all])
            (get-generators gen/int [drop take])
            (get-generators (test-helpers/function gen/boolean)
                            [drop-while filter remove take-while])
            (get-generators (test-helpers/function test-helpers/any-equal)
                            [map map-indexed partition-by])
            (get-generators (test-helpers/function any-nilable-equal)
                            [keep keep-indexed])
            [(gen/fmap replace (gen/map test-helpers/any-equal
                                        test-helpers/any-equal))
             (gen/fmap interpose test-helpers/any-equal)]
            ;Composing mapcat more than once seems to make the test to run longer than 10 seconds.
            ;[(gen/fmap mapcat (test-helpers/function (gen/vector test-helpers/any-equal)))]
            )))

(def xform
  (->> xform*
       gen/vector
       gen/not-empty
       (gen/fmap (partial apply comp))))

(defn get-elements
  [xf earliests as]
  (maybe/map-maybe (partial (comp unreduced
                                  (xf (comp maybe/just
                                            second
                                            vector)))
                            helpers/nothing)
                   (concat earliests as)))

(clojure-test/defspec
  transduce-identity
  test-helpers/cljc-num-tests
  ;TODO refactor
  (test-helpers/restart-for-all
    [input-event test-helpers/event
     xf xform
     f (test-helpers/function test-helpers/any-equal)
     init test-helpers/any-equal
     as (gen/vector test-helpers/any-equal)]
    (let [transduced-event (frp/transduce xf f init input-event)
          earliests @input-event]
      (frp/activate)
      (run! input-event as)
      (->> as
           (get-elements xf (map tuple/snd earliests))
           (reductions f init)
           rest
           (= (map tuple/snd @transduced-event))))))

(clojure-test/defspec
  cat-identity
  test-helpers/cljc-num-tests
  ;TODO refactor
  (test-helpers/restart-for-all
    ;TODO generate an event with pure
    [input-event (gen/fmap (fn [_]
                             (frp/event))
                           (gen/return unit/unit))
     f (test-helpers/function test-helpers/any-equal)
     init test-helpers/any-equal
     ;TODO generate list
     as (gen/vector (gen/vector test-helpers/any-equal))]
    ;TODO compose xforms
    (let [cat-event (frp/transduce cat f init input-event)
          map-event (frp/transduce (comp (remove empty?)
                                         (map last))
                                   f
                                   init
                                   input-event)]
      (frp/activate)
      (run! input-event as)
      (= @cat-event @map-event))))

;TODO test snapshot
