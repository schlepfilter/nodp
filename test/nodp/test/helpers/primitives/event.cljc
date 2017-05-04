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
  ;TODO refactor
  (gen/let [probabilities (gen/sized (comp (partial gen/vector
                                                    test-helpers/probability
                                                    3)
                                           (partial + 3)))
            [outer-input-event & inner-input-events :as input-events]
            (gen/return (test-helpers/get-events probabilities))
            ;TODO generalize gen/uuid
            fs (gen/vector (test-helpers/function gen/uuid)
                           (count input-events))
            input-event-anys (gen/vector gen/uuid
                                         ((if (empty? @outer-input-event)
                                            identity
                                            dec)
                                           (dec (count input-events))))
            calls (gen/shuffle
                    (concat (map (fn [a]
                                   (fn []
                                     (outer-input-event a)))
                                 input-event-anys)
                            (map (fn [inner-input-event as]
                                   (fn []
                                     (if (not= inner-input-event
                                               outer-input-event)
                                       (run! inner-input-event as))))
                                 inner-input-events
                                 (gen/vector (gen/vector test-helpers/any-equal)
                                             (count inner-input-events)))))]
           (gen/tuple (gen/return (doall (map nodp.helpers/<$>
                                              fs
                                              input-events)))
                      (gen/return (partial doall (map helpers/funcall
                                                      calls))))))

(clojure-test/defspec
  event->>=-identity
  test-helpers/num-tests
  (test-helpers/restart-for-all
    [[[outer-event & inner-events] calls] event->>=]
    (let [bound-event (helpers/>>= outer-event
                                   (test-helpers/make-iterate inner-events))]
      (frp/activate)
      (calls)
      true)))
