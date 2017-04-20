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

(clojure-test/defspec
  first-theorem
  test-helpers/num-tests
  (test-helpers/restart-for-all
    [original-behavior test-helpers/continuous-behavior
     integration-method (gen/elements [:trapezoid])
     lower-limit-value (gen/fmap #?(:clj  numeric-tower/abs
                                    :cljs js/Math.abs) gen/ratio)
     n gen/pos-int]
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
      (dotimes [_ n]
        (e unit/unit))
      (let [latest @integral-behavior]
        (e unit/unit)
        (cond (< @@frp/time lower-limit-value)
              (= @integral-behavior helpers/nothing)
              (= @@frp/time lower-limit-value) (= @@integral-behavior 0)
              :else (or (maybe/nothing? latest)
                        (= latest @integral-behavior)))))))

#?(:clj (clojure-test/defspec
          second-theorem
          test-helpers/num-tests
          (test-helpers/restart-for-all
            [original-behavior (gen/fmap frp/behavior gen/ratio)]
            (let [derivative-behavior (->> original-behavior
                                           (frp/integral
                                             :trapezoid
                                             (time/time 0))
                                           (helpers/<$> deref)
                                           frp/derivative)]
              (frp/activate)
              (= @original-behavior @@derivative-behavior)))))

;TODO test integral and derivative with linear functions of time