(ns nodp.test.helpers.io
  (:require [cats.monad.maybe :as maybe]
            [clojure.test.check]
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
  #?(:cljs (:require-macros [nodp.test.helpers.io :refer [with-exit
                                                          with-exitv]])))

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
  test-helpers/num-tests
  (prop/for-all [as (gen/vector test-helpers/any-equal)
                 b test-helpers/any-equal]
                (= (with-exitv exit
                               (->> as
                                    (map exit)
                                    doall)
                               b)
                   as)))

(defn get-occurrence-values
  [e as]
  (maybe/maybe as
               @e
               (fn [x]
                 (s/setval s/BEGINNING
                           (vector (tuple/snd x))
                           as))))

(clojure-test/defspec
  event-on-identity
  test-helpers/num-tests
  (test-helpers/restart-for-all
    [e test-helpers/event
     as (gen/vector test-helpers/any-equal)]
    (let [occurrence-values (get-occurrence-values e as)]
      (= (with-exitv exit
                     (frp/on exit e)
                     (frp/activate)
                     (run! e as))
         occurrence-values))))

#?(:clj (defmacro with-exit
          [exit-name & body]
          `(last (with-exitv ~exit-name ~@body))))

(clojure-test/defspec
  with-exit-identity
  test-helpers/num-tests
  (prop/for-all [a test-helpers/any-equal
                 b test-helpers/any-equal]
                (= (with-exit exit
                              (exit a)
                              b)
                   a)))

(clojure-test/defspec
  behavior-on-identity
  test-helpers/num-tests
  (test-helpers/restart-for-all
    [e test-helpers/event
     as (-> test-helpers/any-equal
            gen/vector
            gen/not-empty)]
    (let [b (frp/stepper (first as) e)
          occurrence-values (get-occurrence-values e as)]
      (= (with-exit exit
                    (frp/on exit b)
                    (frp/activate)
                    (run! e (rest occurrence-values)))
         (last occurrence-values)))))
