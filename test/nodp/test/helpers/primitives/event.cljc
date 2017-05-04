(ns nodp.test.helpers.primitives.event
  (:require [clojure.test.check]
            [clojure.test.check.clojure-test
             :as clojure-test
             :include-macros true]
            [clojure.test.check.generators :as gen]
            [nodp.helpers.frp :as frp]
            [nodp.test.helpers :as test-helpers :include-macros true]))

(clojure-test/defspec
  call-inactive
  test-helpers/num-tests
  (test-helpers/restart-for-all [as (gen/vector test-helpers/any-equal)]
                                (let [e (frp/event)]
                                  (run! e as)
                                  (= @e []))))

(clojure-test/defspec
  call-active
  test-helpers/num-tests
  (test-helpers/restart-for-all [as (gen/vector test-helpers/any-equal)]
                                (let [e (frp/event)]
                                  (frp/activate)
                                  (run! e as)
                                  true)))