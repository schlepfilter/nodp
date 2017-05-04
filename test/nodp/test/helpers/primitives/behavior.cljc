(ns nodp.test.helpers.primitives.behavior
  (:require [clojure.test.check.clojure-test
             :as clojure-test
             :include-macros true]
            [nodp.helpers :as helpers]
            [nodp.helpers.frp :as frp]
            [nodp.helpers.unit :as unit]
            [nodp.test.helpers :as test-helpers]))

(clojure-test/defspec
  behavior-return
  test-helpers/num-tests
  (test-helpers/restart-for-all [a test-helpers/any-equal]
                                (= @(-> unit/unit
                                        frp/behavior
                                        helpers/infer
                                        (nodp.helpers/return a))
                                   a)))
