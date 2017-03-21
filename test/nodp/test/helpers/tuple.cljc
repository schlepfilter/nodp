(ns nodp.test.helpers.tuple
  (:require [cats.core :as m]
            [cats.monad.maybe :as maybe]
            [clojure.test.check]
            [clojure.test.check.clojure-test :as clojure-test :include-macros true]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [nodp.helpers :as helpers]
            [nodp.helpers.tuple :as tuple]
            [nodp.helpers.unit :as unit]))

(def function
  (gen/fmap (fn [_]
              (memoize (fn [_]
                         (gen/generate gen/any))))
            (gen/return unit/unit)))

(defn maybe
  [generator]
  (gen/bind generator
            (fn [a]
              (gen/one-of [(gen/return (maybe/nothing))
                           (gen/return (maybe/just a))]))))

(def monoid-scalar
  (gen/one-of [gen/string
               (gen/return unit/unit)
               (gen/vector gen/any)
               (gen/list gen/any)
               (gen/set gen/any)
               (gen/map gen/any gen/any)]))

(def monoid
  (gen/one-of [monoid-scalar
               (gen/recursive-gen maybe monoid-scalar)]))

(def mempty
  (gen/fmap (comp m/mempty helpers/infer)
            monoid))

(clojure-test/defspec
  tuple-monad-right-identity-law
  10
  (prop/for-all [a gen/any
                 mempty* mempty]
                (= (m/>>= (tuple/tuple mempty* a) m/return)
                   (tuple/tuple mempty* a))))

(clojure-test/defspec
  tuple-monad-left-identity-law
  10
  (prop/for-all [a gen/any
                 f function
                 monoid* monoid]
                (let [get-monadic-value (comp (partial tuple/tuple monoid*)
                                              f)]
                  (= (m/>>= (tuple/tuple (-> monoid*
                                             helpers/infer
                                             m/mempty)
                                         a)
                            get-monadic-value)
                     (get-monadic-value a)))))
