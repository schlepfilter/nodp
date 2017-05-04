(ns nodp.test.helpers.primitives.event
  (:require [#?(:clj  clojure.test
                :cljs cljs.test) :as test :include-macros true]
            [clojure.test.check]
            [clojure.test.check.clojure-test
             :as clojure-test
             :include-macros true]
            [clojure.test.check.generators :as gen]
            [nodp.helpers :as helpers]
            [nodp.helpers.frp :as frp]
            [nodp.helpers.time :as time]
            [nodp.helpers.tuple :as tuple]
            [nodp.test.helpers :as test-helpers :include-macros true]))

(test/use-fixtures :each test-helpers/fixture)

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
                                  (= (map tuple/snd @e) as))))

(clojure-test/defspec
  event-return
  test-helpers/num-tests
  (test-helpers/restart-for-all [a test-helpers/any-equal]
                                (= (last @(-> (frp/event)
                                              helpers/infer
                                              (nodp.helpers/return a)))
                                   (-> 0
                                       time/time
                                       (tuple/tuple a)))))

(def event->>=
  ;TODO return a tuple
  (gen/let [probabilities (gen/sized (comp (partial gen/vector
                                                    test-helpers/probability
                                                    3)
                                           (partial + 3)))]))

(clojure-test/defspec
  event->>=-identity
  test-helpers/num-tests
  (test-helpers/restart-for-all [_ event->>=]
                                true))