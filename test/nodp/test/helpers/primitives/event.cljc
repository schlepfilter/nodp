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
  (gen/let [probabilities (gen/sized (comp (partial gen/vector
                                                    test-helpers/probability
                                                    3)
                                           (partial + 3)))
            [outer-input-event & inner-input-events :as input-events]
            (gen/return (test-helpers/get-events probabilities))
            ;TODO generalize gen/uuid
            fs (gen/vector (test-helpers/function gen/uuid)
                           (count input-events))
            input-event-anys (gen/vector gen/uuid
                                         ((if (maybe/just? @outer-input-event)
                                            dec
                                            identity)
                                           (dec (count input-events))))
            calls (gen/shuffle
                    (concat (map (fn [a]
                                   (fn []
                                     (outer-input-event a)))
                                 input-event-anys)
                            (map (fn [inner-input-event as]
                                   (fn []
                                     (if (not= inner-input-event
                                               outer-input-event)
                                       (run! inner-input-event as))))
                                 inner-input-events
                                 (gen/vector (gen/vector test-helpers/any-equal)
                                             (count inner-input-events)))))]
           (gen/tuple (gen/return (doall (map nodp.helpers/<$>
                                              fs
                                              input-events)))
                      (gen/return (partial doall (map helpers/funcall
                                                      (drop-last calls))))
                      (gen/return (last calls))
                      (gen/return ((if (maybe/just? @outer-input-event)
                                     inc
                                     identity)
                                    (count input-event-anys))))))

(defn all-nothing?
  [e es]
  (and (maybe/nothing? @e)
       (not-any? maybe/just? (map deref es))))

(def get-tuples
  (comp maybe/cat-maybes
        (partial map deref)))

(defn right-biased?*
  [e es]
  (->> es
       get-tuples
       (filter (comp (partial = (tuple/fst @@e))
                     tuple/fst))
       last
       tuple/snd
       (= (tuple/snd @@e))))

(def right-biased?
  (helpers/build or
                 all-nothing?
                 right-biased?*))

(defn right-most-latest?
  [bound-event inner-events]
  (->> inner-events
       get-tuples
       (filter (comp (partial = (apply (partial max-key deref)
                                       (->> inner-events
                                            get-tuples
                                            (map tuple/fst))))
                     tuple/fst))
       last
       tuple/snd
       (= (tuple/snd @@bound-event))))

(clojure-test/defspec
  event->>=-identity
  test-helpers/num-tests
  (test-helpers/restart-for-all
    [[[outer-event & inner-events] calls call n] event->>=]
    (let [bound-event (helpers/>>= outer-event
                                   (test-helpers/make-iterate inner-events))]
      (frp/activate)
      (calls)
      (let [outer-latest @outer-event
            inner-latests (doall (map deref inner-events))
            bound-latest @bound-event]
        (call)
        (if (or (maybe/nothing? @(last inner-events))
                (= @outer-event outer-latest))
          (if (= (map deref inner-events) inner-latests)
            (= @bound-event bound-latest)
            (right-most-latest? bound-event inner-events))
          (and (= (tuple/snd @@bound-event) (tuple/snd @@(last inner-events)))
               (<= @(tuple/fst @@(last inner-events))
                   @(tuple/fst @@bound-event))))))))

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
                                (right-biased? mappended-event fmapped-events)))

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

(defn get-elements
  [xf earliest as]
  (->> (comp (partial (helpers/flip cons) as)
             tuple/snd)
       (maybe/maybe as earliest)
       (maybe/map-maybe (partial (comp unreduced
                                       (xf (comp maybe/just
                                                 second
                                                 vector)))
                                 helpers/nothing))))

(clojure-test/defspec
  transduce-identity
  test-helpers/num-tests
  ;TODO refactor
  (test-helpers/restart-for-all
    [input-event (gen/no-shrink test-helpers/event)
     xf (gen/no-shrink xform)
     f (gen/no-shrink (test-helpers/function test-helpers/any-equal))
     init (gen/no-shrink test-helpers/any-equal)
     as (gen/no-shrink (gen/vector test-helpers/any-equal))]
    (let [transduced-event (frp/transduce xf f init input-event)
          earliest @input-event]
      (frp/activate)
      (run! input-event as)
      (if (empty? (get-elements xf earliest as))
        (= @transduced-event helpers/nothing)
        (= (tuple/snd @@transduced-event)
           (reduce f init (get-elements xf earliest as)))))))
