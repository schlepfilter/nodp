(ns nodp.test.helpers.derived.behavior
  (:require [clojure.test.check]
            [clojure.test.check.clojure-test
             :as clojure-test
             :include-macros true]
            [clojure.test.check.generators :as gen]
    #?(:clj
            [clojure.math.numeric-tower :as numeric-tower])
            [#?(:clj  clojure.test
                :cljs cljs.test) :as test :include-macros true]
            [nodp.helpers.frp :as frp]
            [nodp.helpers :as helpers]
            [nodp.helpers.time :as time]
            [nodp.helpers.unit :as unit]
            [nodp.test.helpers :as test-helpers :include-macros true]))

(test/use-fixtures :each test-helpers/fixture)

(def invoke
  (gen/let [a test-helpers/any-equal]
           ;TODO randomize the function that may be lifted to include functions that won't be lifted
           (= @(frp/lifting ((fn [& _]
                               a)
                              ;TODO randomize the numbers and values of behaviors and constants
                              ;TODO randomize how behavior is created
                              (frp/behavior unit/unit)))
              a)))

(clojure-test/defspec
  lifting-invoke
  test-helpers/num-tests
  (test-helpers/restart-for-all [invoke* invoke]
                                invoke*))

(def rational-base
  (gen/one-of [gen/s-pos-int gen/s-neg-int]))

(def rational-continuous-behavior
  (test-helpers/behavior test-helpers/polynomial
                         (test-helpers/exponential rational-base)))

(clojure-test/defspec
  first-theorem
  test-helpers/num-tests
  (test-helpers/restart-for-all
    [original-behavior rational-continuous-behavior
     integration-method (gen/elements [:left :right :trapezoid])
     lower-limit-value (gen/fmap #?(:clj  numeric-tower/abs
                                    :cljs js/Math.abs)
                                 gen/ratio)
     advance* test-helpers/advance]
    (let [minuend-behavior (frp/integral integration-method
                                         (time/time 0)
                                         original-behavior)
          subtrahend-behavior (frp/integral integration-method
                                            (time/time lower-limit-value)
                                            original-behavior)
          integral-behavior ((helpers/lift-a 2
                                             (fn [x y]
                                               ;TODO remove with-redefs after cats.context is fixed
                                               (with-redefs [cats.context/infer
                                                             helpers/infer]
                                                 ((helpers/lift-a 2 -) x y))))

                              minuend-behavior
                              subtrahend-behavior)
          e (frp/event)]
      (frp/activate)
      (advance*)
      (let [latest @integral-behavior]
        (e unit/unit)
        ;TODO remove this reader conditional if Ratio gets supported in ClojureScript
        ;
        ;ClojureScript currently only supports integer and floating point literals that map to JavaScript primitives
        ;Ratio, BigDecimal, and BigInteger literals are currently not supported
        ;https://github.com/clojure/clojurescript/wiki/Differences-from-Clojure
        #?(:clj  (cond (< @@frp/time lower-limit-value)
                       (= @integral-behavior helpers/nothing)
                       (= @@frp/time lower-limit-value)
                       (and (= latest helpers/nothing)
                            (= @integral-behavior @minuend-behavior)
                            (= @@subtrahend-behavior 0))
                       :else (or (= latest helpers/nothing)
                                 (= latest @integral-behavior)))
           :cljs true)))))

(clojure-test/defspec
  integral-constant
  test-helpers/num-tests
  (test-helpers/restart-for-all
    [constant-behavior (gen/fmap frp/behavior gen/ratio)
     advance* test-helpers/advance]
    (let [integral-behavior (frp/integral :trapezoid
                                          (time/time 0)
                                          constant-behavior)]
      (frp/activate)
      (advance*)
      ;TODO remove this reader conditional if Ratio gets supported in ClojureScript
      #?(:clj  (= @@integral-behavior
                  (* @constant-behavior @@frp/time))
         :cljs true))))

(clojure-test/defspec
  derivative-linear
  test-helpers/num-tests
  (test-helpers/restart-for-all
    [[_ coefficient :as coefficients] (gen/vector gen/ratio 2)
     advance* test-helpers/advance]
    (let [linear-behavior (helpers/<$>
                            (comp (partial test-helpers/get-polynomial
                                           0
                                           coefficients)
                                  deref)
                            frp/time)
          derivative-behavior (frp/derivative linear-behavior)]
      (frp/activate)
      (advance*)
      ;TODO remove this reader conditional if Ratio gets supported in ClojureScript
      #?(:clj  (= coefficient @@derivative-behavior)
         :cljs true))))
