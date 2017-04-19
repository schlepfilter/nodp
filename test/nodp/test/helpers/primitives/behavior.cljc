(ns nodp.test.helpers.primitives.behavior
  (:require [cats.monad.maybe :as maybe]
            [clojure.math.numeric-tower :as numeric-tower]
            [clojure.test.check]
            [clojure.test.check.clojure-test
             :as clojure-test
             :include-macros true]
            [clojure.test.check.generators :as gen]
            [#?(:clj  clojure.test
                :cljs cljs.test) :as test :include-macros true]
            [nodp.helpers :as helpers]
            [nodp.helpers.frp :as frp]
            [nodp.helpers.time :as time]
            [nodp.helpers.tuple :as tuple]
            [nodp.helpers.unit :as unit]
            [nodp.test.helpers :as test-helpers :include-macros true]))

(test/use-fixtures :each test-helpers/fixture)

(clojure-test/defspec
  behavior-return
  test-helpers/num-tests
  (test-helpers/restart-for-all [a test-helpers/any-equal]
                                (= @(-> unit/unit
                                        frp/behavior
                                        helpers/infer
                                        (nodp.helpers/return a))
                                   a)))

(clojure-test/defspec
  time-increasing
  test-helpers/num-tests
  (test-helpers/restart-for-all
    [units (gen/vector (gen/return unit/unit))]
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
  (gen/let [probabilities (gen/sized (comp (partial gen/vector
                                                    test-helpers/probability
                                                    3)
                                           (partial + 3)))
            [[input-event & input-events]
             [fmapped-switching-event
              fmapped-outer-event
              & fmapped-inner-events]
             n]
            (test-helpers/events-tuple probabilities)
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
                          (test-helpers/make-iterate inner-behaviors)
                          fmapped-switching-event))
            input-event-anys (gen/vector test-helpers/any-equal n)
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
  test-helpers/num-tests
  (test-helpers/restart-for-all
    [[outer-behavior e switched-behavior call] (gen/no-shrink switcher)]
    (if (maybe/nothing? @e)
      (= @switched-behavior @outer-behavior)
      (= @switched-behavior @(tuple/snd @@e)))))

(def behavior->>=
  ;TODO refactor
  (gen/let [probabilities (gen/vector test-helpers/probability 2)
            [[input-outer-event input-inner-event]
             [fmapped-outer-event fmapped-inner-event]]
            (test-helpers/events-tuple probabilities)
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
  test-helpers/num-tests
  (test-helpers/restart-for-all
    [[outer-behavior get-behavior call] behavior->>=]
    (let [bound-behavior (helpers/>>= outer-behavior get-behavior)]
      (frp/activate)
      (call)
      (= @bound-behavior @(get-behavior @outer-behavior)))))

(def big-decimal
  (gen/fmap (fn [x]
              (BigDecimal. x))
            (gen/double* {:NaN? false
                          :min  -100.0
                          :max  100.0})))

(defn get-polynomial
  [coefficients x]
  (reduce-kv (fn [init k v]
               (+ init (* v (numeric-tower/expt x k))))
             0
             coefficients))

(def polynomial
  (gen/let [coefficients (gen/vector big-decimal)]
           (partial get-polynomial coefficients)))

(def continuous
  (gen/let [polynomial* polynomial]
           ;TODO generate algebraic operations to perform on the behavior
           (gen/return (helpers/<$> (comp polynomial*
                                          deref)
                                    frp/time))))

(clojure-test/defspec
  first-theorem
  test-helpers/num-tests
  (test-helpers/restart-for-all
    [original-behavior continuous
     lower-limit-value (gen/double* {:NaN? false
                                     :min  0})
     n gen/pos-int]
    (let [integral-behavior ((helpers/lift-a 2
                                             (fn [x y]
                                               ;TODO remove with-redefs after cats.context is fixed
                                               (with-redefs [cats.context/infer
                                                             helpers/infer]
                                                 ((helpers/lift-a 2 -) x y))))
                              (frp/integral (time/time 0) original-behavior)
                              (frp/integral (time/time lower-limit-value)
                                            original-behavior))
          e (frp/event)]
      (frp/activate)
      (dotimes [_ n]
        (e unit/unit))
      (let [latest @integral-behavior]
        (e unit/unit)
        (cond (< @@frp/time lower-limit-value)
              (= @integral-behavior helpers/nothing)
              (= @@frp/time lower-limit-value) (= @@integral-behavior 0)
              :else (or (maybe/nothing? latest)
                        (= latest @integral-behavior)))))))

(clojure-test/defspec
  second-theorem
  test-helpers/num-tests
  (test-helpers/restart-for-all
    [original-behavior (gen/fmap frp/behavior gen/ratio)]
    (let [derivative-behavior (->> original-behavior
                                   (frp/integral (time/time 0))
                                   (helpers/<$> deref)
                                   frp/derivative)]
      (frp/activate)
      (= @original-behavior @@derivative-behavior))))

;TODO test integral and derivative with linear functions of time
