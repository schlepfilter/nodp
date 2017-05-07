(ns nodp.test.helpers.io
  (:require [clojure.test.check]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop :include-macros true]
            [clojure.test.check.clojure-test
             :as clojure-test
             :include-macros true]
            [#?(:clj  clojure.test
                :cljs cljs.test) :as test :include-macros true]
            [com.rpl.specter :as s]
            [nodp.helpers :as helpers]
            [nodp.helpers.frp :as frp]
            [nodp.helpers.tuple :as tuple]
            [nodp.test.helpers :as test-helpers :include-macros true]
    #?(:clj
            [riddley.walk :as walk]))
  #?(:cljs (:require-macros [nodp.test.helpers.io :refer [with-exitv]])))

(test/use-fixtures :each test-helpers/fixture)

#?(:clj (defmacro with-exitv
          [exit-name & body]
          (potemkin/unify-gensyms
            `(let [exits-state## (atom [])]
               ~(walk/walk-exprs
                  (partial = exit-name)
                  (fn [_#]
                    `(comp (partial swap! exits-state##)
                           (helpers/curry 2 (helpers/flip conj))))
                  (cons `do body))
               @exits-state##))))

(clojure-test/defspec
  with-exitv-identity
  test-helpers/cljc-num-tests
  (prop/for-all [as (gen/vector test-helpers/any-equal)
                 b test-helpers/any-equal]
                (= (with-exitv exit
                               (->> as
                                    (map exit)
                                    doall)
                               b)
                   as)))

(clojure-test/defspec
  behavior-on
  test-helpers/cljc-num-tests
  (test-helpers/restart-for-all
    [e test-helpers/event
     a test-helpers/any-equal
     as (gen/vector test-helpers/any-equal)]
    (let [b (frp/stepper a e)]
      (= (into [] (dedupe (concat [a]
                                  (remove (partial = a)
                                          (map tuple/snd @e))
                                  as)))
         (with-exitv exit
                     (frp/on exit b)
                     (frp/activate)
                     (run! e as))))))
