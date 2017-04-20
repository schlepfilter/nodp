(ns nodp.test.helpers.derived.behavior
  (:require [cats.monad.maybe :as maybe]
            [clojure.test.check]
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

(def rational-base
  (gen/one-of [gen/s-pos-int gen/s-neg-int]))

(def rational-continuous-behavior
  (test-helpers/behavior test-helpers/polynomial
                         (test-helpers/exponential rational-base)))

(def advance
  (gen/let [n gen/pos-int]
           (let [e (frp/event)]
             (fn []
               (dotimes [_ n]
                 (e unit/unit))))))

(clojure-test/defspec
  first-theorem
  test-helpers/num-tests
  (test-helpers/restart-for-all
    [original-behavior rational-continuous-behavior
     integration-method (gen/elements [:left :right :trapezoid])
     lower-limit-value (gen/fmap #?(:clj  numeric-tower/abs
                                    :cljs js/Math.abs)
                                 gen/ratio)
     advance* advance]
    (let [integral-behavior ((helpers/lift-a 2
                                             (fn [x y]
                                               ;TODO remove with-redefs after cats.context is fixed
                                               (with-redefs [cats.context/infer
                                                             helpers/infer]
                                                 ((helpers/lift-a 2 -) x y))))
                              (frp/integral integration-method
                                            (time/time 0)
                                            original-behavior)
                              (frp/integral integration-method
                                            (time/time lower-limit-value)
                                            original-behavior))
          e (frp/event)]
      (frp/activate)
      (advance*)
      (let [latest @integral-behavior]
        (e unit/unit)
        (cond (< @@frp/time lower-limit-value)
              (= @integral-behavior helpers/nothing)
              (= @@frp/time lower-limit-value) (= @@integral-behavior 0)
              :else (or (maybe/nothing? latest)
                        (= latest @integral-behavior)))))))

(clojure-test/defspec
  integral-constant
  test-helpers/num-tests
  (test-helpers/restart-for-all
    [constant-behavior (gen/fmap frp/behavior gen/ratio)
     advance* advance]
    (let [integral-behavior (frp/integral :trapezoid
                                          (time/time 0)
                                          constant-behavior)]
      (frp/activate)
      (advance*)
      (= @@integral-behavior
         (* @constant-behavior @@frp/time)))))

(clojure-test/defspec
  derivative-linear
  test-helpers/num-tests
  (test-helpers/restart-for-all
    [[_ coefficient :as coefficients] (gen/vector gen/ratio 2)
     advance* advance]
    (let [linear-behavior (helpers/<$>
                            (comp (partial test-helpers/get-polynomial
                                           coefficients)
                                  deref)
                            frp/time)
          derivative-behavior (frp/derivative linear-behavior)]
      (frp/activate)
      (advance*)
      (= coefficient @@derivative-behavior))))
