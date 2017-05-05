(ns nodp.test.helpers.primitives.behavior
  (:require [clojure.test.check]
            [clojure.test.check.clojure-test
             :as clojure-test
             :include-macros true]
            [clojure.test.check.generators :as gen]
            [nodp.helpers :as helpers]
            [nodp.helpers.frp :as frp]
            [nodp.helpers.tuple :as tuple]
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
(def switcher
  ;TODO refactor
  (gen/let [probabilities (gen/sized (comp (partial gen/vector
                                                    test-helpers/probability
                                                    4)
                                           (partial + 4)))
            [[input-event & input-events]
             [fmapped-switching-event
              fmapped-outer-event
              & fmapped-inner-events]]
            (test-helpers/events-tuple probabilities)
            [stepper-outer-any input-outer-any]
            (gen/vector test-helpers/any-equal 2)
            outer-behavior
            ;TODO generate time
            (gen/elements [(frp/stepper stepper-outer-any
                                        fmapped-outer-event)])
            stepper-inner-anys (gen/vector test-helpers/any-equal
                                           (count fmapped-inner-events))
            steps (gen/vector gen/boolean (count fmapped-inner-events))
            inner-behaviors
            (gen/return
              (doall
                (map (fn [step stepping-inner-any fmapped-inner-event]
                       ;TODO generate time
                       (frp/stepper stepping-inner-any
                                    fmapped-inner-event))
                     steps
                     stepper-inner-anys
                     fmapped-inner-events)))
            switching-event
            (gen/return (helpers/<$>
                          (test-helpers/make-iterate inner-behaviors)
                          fmapped-switching-event))
            input-event-anys (gen/vector test-helpers/any-equal
                                         ((if (empty? @switching-event)
                                            identity
                                            dec)
                                           (count fmapped-inner-events)))
            input-events-anys (gen/vector test-helpers/any-equal
                                          (count input-event-anys))
            calls (gen/shuffle (concat (map (fn [a]
                                              (fn []
                                                (input-event a)))
                                            input-event-anys)
                                       (map (fn [a input-event*]
                                              (fn []
                                                (if-not (= input-event*
                                                           input-event)
                                                  (input-event* a))))
                                            input-events-anys
                                            input-events)))]
           (gen/tuple (gen/return outer-behavior)
                      (gen/return switching-event)
                      (gen/return (frp/switcher outer-behavior
                                                switching-event))
                      (gen/return (partial doall (map (fn [call]
                                                        (call))
                                                      (drop-last calls))))
                      (gen/return (last calls)))))

(clojure-test/defspec
  switcher-identity
  test-helpers/num-tests
  (test-helpers/restart-for-all
    ;TODO refactor
    [[outer-behavior e switched-behavior calls call] (gen/no-shrink switcher)]
    (frp/activate)
    (calls)
    (let [occs @e]
      (call)
      (if (= @e occs)
        true
        (= @switched-behavior @(tuple/snd (last (drop-last @e))))))))
