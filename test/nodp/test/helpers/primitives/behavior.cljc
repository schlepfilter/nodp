(ns nodp.test.helpers.primitives.behavior
  (:require [cats.monad.maybe :as maybe]
            [clojure.test.check]
            [clojure.test.check.clojure-test
             :as clojure-test
             :include-macros true]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop :include-macros true]
            [#?(:clj  clojure.test
                :cljs cljs.test) :as test :include-macros true]
    #?(:clj
            [clojure.math.numeric-tower :as numeric-tower])
    #?(:clj
            [incanter.core :as incanter])
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
              & fmapped-inner-events]]
            (test-helpers/events-tuple probabilities)
            [stepper-outer-any input-outer-any]
            (gen/vector test-helpers/any-equal 2)
            outer-behavior
            (gen/elements [(frp/stepper stepper-outer-any
                                        fmapped-outer-event)
                           frp/time])
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
            input-event-anys (gen/vector test-helpers/any-equal)
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
    [[outer-behavior e switched-behavior call] switcher]
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
            outer-behavior (gen/elements [(frp/stepper outer-any
                                                       fmapped-outer-event)
                                          frp/time])
            inner-any test-helpers/any-equal
            f (gen/elements [frp/behavior
                             (constantly (frp/stepper inner-any
                                                      fmapped-inner-event))
                             (constantly frp/time)])
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

(def real-trigonometric
  (gen/elements #?(:clj  [incanter/sin incanter/cos incanter/atan]
                   :cljs [js/Math.sin js/Math.cos js/Math.atan])))

(def logarithmic
  (gen/return (comp #?(:clj  incanter/log
                       :cljs js/Math.log)
                    inc)))

(def real-base
  (gen/one-of [(gen/double* {:NaN?      false
                             :infinite? false
                             :max       -1})
               (gen/double* {:NaN?      false
                             :infinite? false
                             :min       1})]))

(def formal-laurent
  (gen/let [offset (gen/sized (comp gen/return -))
            coefficients (gen/vector gen/ratio)]
           (comp (partial test-helpers/get-polynomial offset coefficients)
                 inc)))

(def continuous-behavior
  (test-helpers/behavior test-helpers/polynomial
                         formal-laurent
                         (test-helpers/exponential real-base)
                         logarithmic
                         real-trigonometric))

(def pos-rational
  (gen/fmap #?(:clj  numeric-tower/abs
               :cljs js/Math.abs)
            gen/ratio))

(def calculus
  (gen/let [k (gen/elements [:current-latest :current-time :past-time])
            lower-limit-number pos-rational
            x gen/ratio
            e (gen/return (frp/event))
            original-behavior (gen/one-of [continuous-behavior
                                           (gen/return (frp/stepper x e))])
            xs (gen/vector gen/ratio)
            advance* test-helpers/advance]
           (let [calculus-behavior
                 (frp/calculus
                   (case k
                     ;TODO test past-latests and integration
                     :current-latest (fn [current-latest & _]
                                       (maybe/just current-latest))
                     :current-time (fn [_ _ current-time & _]
                                     (maybe/just @current-time))
                     :past-time (fn [_ _ _ past-time _]
                                  (maybe/just @past-time)))
                   (-> lower-limit-number
                       time/time
                       maybe/just)
                   original-behavior)]
             (gen/return
               (fn []
                 (frp/activate)
                 (run! (partial helpers/funcall e) xs)
                 (advance*)
                 (helpers/casep
                   @@frp/time (partial > lower-limit-number)
                   (maybe/nothing? @calculus-behavior)
                   (partial = lower-limit-number)
                   (= @@calculus-behavior 0)
                   (case k
                     :current-latest (= @@calculus-behavior @original-behavior)
                     :current-time (= @@calculus-behavior @@frp/time)
                     :past-time (< @@calculus-behavior @@frp/time))))))))

(clojure-test/defspec
  calculus-identity
  test-helpers/num-tests
  (test-helpers/restart-for-all
    [calculus* calculus]
    (calculus*)))

#?(:clj (clojure-test/defspec
          sample
          test-helpers/num-tests
          (prop/for-all []
                        (frp/restart 1)
                        (frp/activate)
                        (let [t @frp/time]
                          (Thread/sleep 10)
                          (not= @frp/time t)))))
