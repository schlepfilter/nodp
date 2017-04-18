(ns nodp.test.helpers.frp
  (:require [clojure.test.check]
            [clojure.test.check.clojure-test
             :as clojure-test
             :include-macros true]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop :include-macros true]
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
  #?(:cljs (:require-macros [nodp.test.helpers.frp :refer [restart-for-all
                                                           with-exit
                                                           with-exitv]])))

(defn fixture
  [f]
  (reset! helpers/network-state nil)
  (with-redefs [event/queue helpers/funcall]
    (f)))

(test/use-fixtures :each fixture)

(def num-tests
  #?(:clj  10
     :cljs 2))

(def restart
  (gen/fmap (fn [_]
              (frp/restart))
            (gen/return unit/unit)))

#?(:clj (defmacro restart-for-all
          [bindings & body]
          ;TODO generate times and redefine get-new-time
          `(prop/for-all ~(concat `[_# restart]
                                  bindings)
                         ~@body)))

(clojure-test/defspec
  call-inactive
  num-tests
  (restart-for-all [as (gen/vector test-helpers/any-equal)]
                   (let [e (frp/event)]
                     (run! e as)
                     (maybe/nothing? @e))))

(clojure-test/defspec
  call-active
  num-tests
  (restart-for-all [as (gen/vector test-helpers/any-equal)]
                   (let [e (frp/event)]
                     (frp/activate)
                     (run! e as)
                     (= (tuple/snd @@e) (last as)))))

(def probability
  (gen/double* {:max  1
                :min  0
                :NaN? false}))

(clojure-test/defspec
  event-return
  num-tests
  (restart-for-all [a test-helpers/any-equal]
                   (= @@(-> (frp/event)
                            helpers/infer
                            (nodp.helpers/return a))
                      (-> 0
                          time/time
                          (tuple/tuple a)))))

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
  num-tests
  (prop/for-all [a test-helpers/any-equal
                 b test-helpers/any-equal]
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
  num-tests
  (prop/for-all [as (gen/vector test-helpers/any-equal)
                 b test-helpers/any-equal]
                (= (with-exitv exit
                               (->> as
                                    (map exit)
                                    doall)
                               b)
                   as)))

(clojure-test/defspec
  on-identity
  num-tests
  (restart-for-all [as (gen/vector test-helpers/any-equal)]
                   (= (with-exitv exit
                                  (let [e (frp/event)]
                                    (frp/on exit e)
                                    (frp/activate)
                                    (run! e as)))
                      as)))

(def event
  ;gen/fmap ensures a new event is returned
  ;(gen/sample (gen/return (rand)) 2)
  ;=> (0.7306051862977597 0.7306051862977597)
  ;(gen/sample (gen/fmap (fn [_] (rand))
  ;                      (gen/return 0))
  ;            2)
  ;=> (0.8163040448517938 0.8830449199816961)
  (gen/let [a test-helpers/any-equal]
           (gen/one-of [(gen/return (frp/event))
                        (gen/return (nodp.helpers/pure
                                      (helpers/infer (frp/event))
                                      a))])))

(defn conj-event
  [coll probability*]
  (->> coll
       count
       inc
       (* probability*)
       int
       (if (= 1.0 probability*)
         0)
       (nth (conj coll
                  (test-helpers/generate event {:seed (hash probability*)})))
       (conj coll)))

(def get-events
  (partial reduce conj-event []))

(defn make-iterate
  [coll]
  (let [state (atom coll)]
    (fn [& _]
      (let [result (first @state)]
        (swap! state rest)
        result))))

(defn count-left-duplicates
  [coll]
  (dec (count (filter (partial = (first coll)) coll))))

(defn events-tuple
  [probabilities]
  (gen/let [input-events (gen/return (get-events probabilities))
            fs (gen/vector (test-helpers/function test-helpers/any-equal)
                           (count input-events))]
           (gen/tuple
             (gen/return input-events)
             (gen/return (doall (map nodp.helpers/<$> fs input-events)))
             (gen/return
               (- ((if (maybe/just? @(first input-events))
                     dec
                     identity)
                    (dec (count input-events)))
                  (count-left-duplicates (get-events probabilities)))))))

(def event->>=
  ;TODO refactor
  (gen/let [probabilities (gen/sized (comp (partial gen/vector probability 2)
                                           (partial + 2)))
            [[input-event & input-events] [outer-event & inner-events] n]
            (events-tuple probabilities)
            input-event-anys (gen/vector test-helpers/any-equal
                                         n)
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
                                            input-events)))
            invocations (gen/vector gen/boolean (count calls))]
           (gen/tuple (gen/return outer-event)
                      (gen/return inner-events)
                      (gen/return (partial doall (map (fn [invocation call]
                                                        (if invocation
                                                          (call)))
                                                      invocations
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
       (filter (comp (partial = (apply (partial max-key deref)
                                       (->> es
                                            get-tuples
                                            (map tuple/fst))))
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
  num-tests
  (restart-for-all [[outer-event inner-events calls call] event->>=]
                   (frp/activate)
                   (let [bound-event (helpers/>>= outer-event
                                                  (make-iterate inner-events))]
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
  (gen/let [probabilities (gen/vector probability 2)
            [input-events fmapped-events] (events-tuple probabilities)
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
  num-tests
  (restart-for-all [[call mappended-event fmapped-events] <>]
                   (frp/activate)
                   (call)
                   (left-biased? mappended-event fmapped-events)))

(clojure-test/defspec
  event-mempty
  num-tests
  (restart-for-all [a test-helpers/any-equal]
                   (= @(-> (frp/event)
                           helpers/infer
                           nodp.helpers/mempty)
                      helpers/nothing)))

(defn get-generators
  [generator xforms**]
  (map (partial (helpers/flip gen/fmap) generator) xforms**))

(def xform*
  (gen/one-of
    (concat
      [(gen/return (distinct))
       (gen/return (dedupe))
       (gen/fmap replace (gen/map test-helpers/any-equal
                                  test-helpers/any-equal))]
      (get-generators (test-helpers/function gen/boolean)
                      [drop-while filter remove take-while])
      (get-generators gen/s-pos-int [take-nth partition-all])
      (get-generators gen/int [drop take])
      (get-generators (test-helpers/function test-helpers/any-nilable-equal)
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
  num-tests
  ;TODO refactor
  (restart-for-all [input-event event
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

(clojure-test/defspec
  behavior-return
  num-tests
  (restart-for-all [a test-helpers/any-equal]
                   (= @(-> unit/unit
                           frp/behavior
                           helpers/infer
                           (nodp.helpers/return a))
                      a)))

(clojure-test/defspec
  time-increasing
  num-tests
  (restart-for-all [units (gen/vector (gen/return unit/unit))]
                   (let [e (frp/event)
                         _ ((helpers/lift-a 2 (constantly unit/unit))
                             frp/time
                             (frp/stepper unit/unit e))]
                     (frp/activate)
                     (run! e units)
                     (let [t @frp/time]
                       (e unit/unit)
                       (<= @t @@frp/time)))))

(def switcher
  ;TODO refactor
  (gen/let [probabilities (gen/sized (comp (partial gen/vector probability 3)
                                           (partial + 3)))
            [[input-event & input-events]
             [fmapped-switching-event fmapped-outer-event & fmapped-inner-events]
             n]
            (events-tuple probabilities)
            [stepper-outer-any input-outer-any]
            (gen/vector test-helpers/any-equal 2)
            outer-behavior
            (gen/one-of [(gen/return (frp/stepper stepper-outer-any
                                                  fmapped-outer-event))
                         (gen/return frp/time)])
            stepper-inner-anys (gen/vector test-helpers/any-equal
                                           (count fmapped-inner-events))
            steps (gen/vector gen/boolean (count fmapped-inner-events))
            inner-behaviors
            (gen/return
              (doall
                (map (fn [step stepping-inner-any fmapped-inner-event]
                       (if step
                         (frp/stepper stepping-inner-any
                                      fmapped-inner-event)
                         frp/time))
                     steps
                     stepper-inner-anys
                     fmapped-inner-events)))
            switching-event
            (gen/return (helpers/<$>
                          (make-iterate inner-behaviors)
                          fmapped-switching-event))
            input-event-anys (gen/vector test-helpers/any-equal
                                         n)
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
                                            input-events)))
            invocations (gen/vector gen/boolean (count calls))]
           (gen/tuple (gen/return outer-behavior)
                      (gen/return switching-event)
                      (gen/return (frp/switcher outer-behavior
                                                switching-event))
                      (gen/return (partial doall (map (fn [invocation call]
                                                        (if invocation
                                                          (call)))
                                                      invocations
                                                      (drop-last calls)))))))

(clojure-test/defspec
  switcher-identity
  num-tests
  (restart-for-all [[outer-behavior e switched-behavior call] (gen/no-shrink switcher)]
                   (if (maybe/nothing? @e)
                     (= @switched-behavior @outer-behavior)
                     (= @switched-behavior @(tuple/snd @@e)))))

(def behavior->>=
  ;TODO refactor
  (gen/let [probabilities (gen/vector probability 2)
            [[input-outer-event input-inner-event]
             [fmapped-outer-event fmapped-inner-event]]
            (events-tuple probabilities)
            outer-any test-helpers/any-equal
            outer-behavior (gen/one-of
                             [(gen/return (frp/stepper outer-any
                                                       fmapped-outer-event))
                              (gen/return frp/time)])
            inner-any test-helpers/any-equal
            f (gen/one-of
                [(gen/return frp/behavior)
                 (gen/return (constantly (frp/stepper inner-any
                                                      fmapped-inner-event)))
                 (gen/return (constantly frp/time))])
            [input-outer-anys input-inner-anys]
            (gen/vector (gen/vector test-helpers/any-equal) 2)
            calls (gen/shuffle (concat (map (fn [a]
                                              (fn []
                                                (input-outer-event a)))
                                            input-outer-anys)
                                       (map (fn [a]
                                              (fn []
                                                (input-inner-event a)))
                                            input-inner-anys)))
            invocations (gen/vector gen/boolean (count calls))]
           (gen/tuple (gen/return outer-behavior)
                      (gen/return f)
                      (gen/return (partial doall (map (fn [invocation call]
                                                        (if invocation
                                                          (call)))
                                                      invocations
                                                      calls))))))

(clojure-test/defspec
  behavior->>=-identity
  num-tests
  (restart-for-all [[outer-behavior get-behavior call] behavior->>=]
                   (let [bound-behavior (helpers/>>= outer-behavior
                                                     get-behavior)]
                     (frp/activate)
                     (call)
                     (= @bound-behavior @(get-behavior @outer-behavior)))))

(def calculus
  (gen/let [e event]
           ;TODO generate a stepped behavior
           ;TODO generate algebraic operations to perform on the behavior
           (gen/one-of [(gen/return (helpers/<$> deref frp/time))])))

(clojure-test/defspec
  first-theorem
  num-tests
  (restart-for-all
    [original-behavior calculus
     lower-limit-value (gen/double* {:min 0})]
    (let [integral-behavior ((helpers/lift-a 2
                                             (fn [x y]
                                               (with-redefs [cats.context/infer
                                                             helpers/infer]
                                                 ((helpers/lift-a 2 -) x y))))
                              (frp/integral (time/time 0) original-behavior)
                              (frp/integral (time/time lower-limit-value)
                                            original-behavior))
          e (frp/event)]
      (frp/activate)
      (e unit/unit)
      (let [latest @integral-behavior]
        (e unit/unit)
        (cond (< @@frp/time lower-limit-value)
              (= @integral-behavior helpers/nothing)
              (= @@frp/time lower-limit-value) (= @@integral-behavior 0)
              :else (or (maybe/nothing? latest)
                        (= latest @integral-behavior)))))))

(clojure-test/defspec
  second-theorem
  num-tests
  (restart-for-all [original-behavior calculus]
                   (let [derivative-behavior (->> original-behavior
                                                  (frp/integral (time/time 0))
                                                  (helpers/<$> deref)
                                                  frp/derivative)]
                     (frp/activate)
                     (= @original-behavior @@derivative-behavior))))
