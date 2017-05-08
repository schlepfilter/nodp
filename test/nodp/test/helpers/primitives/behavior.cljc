(ns nodp.test.helpers.primitives.behavior
  (:require [#?(:clj  clojure.test
                :cljs cljs.test) :as test :include-macros true]
            [clojure.test.check]
            [clojure.test.check.clojure-test
             :as clojure-test
             :include-macros true]
            [clojure.test.check.generators :as gen]
            [nodp.helpers :as helpers :include-macros true]
            [nodp.helpers.frp :as frp]
            [nodp.helpers.unit :as unit]
            [nodp.test.helpers :as test-helpers :include-macros true]))

(test/use-fixtures :each test-helpers/fixture)

(clojure-test/defspec
  behavior-return
  test-helpers/cljc-num-tests
  (test-helpers/restart-for-all [a test-helpers/any-equal]
                                (= @(-> unit/unit
                                        frp/behavior
                                        helpers/infer
                                        (nodp.helpers/return a))
                                   a)))

(clojure-test/defspec
  time-increasing
  test-helpers/cljc-num-tests
  (test-helpers/restart-for-all
    [advance1 test-helpers/advance
     advance2 test-helpers/advance]
    (frp/activate)
    (advance1)
    (let [t @frp/time]
      (advance2)
      (<= @t @@frp/time))))

;TODO test stepper
;TODO test >>=
