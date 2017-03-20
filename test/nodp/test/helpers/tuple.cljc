(ns nodp.test.helpers.tuple
  (:require [cats.core :as m]
            [clojure.test.check]
            [clojure.test.check.clojure-test :as clojure-test :include-macros true]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [nodp.helpers.tuple :as tuple]
            [nodp.helpers.unit :as unit]))

(def function
  (gen/fmap (fn [_]
              (memoize (fn [_]
                         (gen/generate gen/any))))
            (gen/return unit/unit)))

(clojure-test/defspec
  tuple-monad-right-identity-law
  10
  (prop/for-all [a gen/any]
                (= (m/>>= (tuple/tuple unit/unit a) m/return)
                   (tuple/tuple unit/unit a))))

(clojure-test/defspec
  tuple-monad-left-identity-law
  10
  (prop/for-all [a gen/any
                 f function]
                (let [get-monadic-value (comp (partial tuple/tuple unit/unit)
                                              f)]
                  (= (m/>>= (tuple/tuple unit/unit a) get-monadic-value)
                     (get-monadic-value a)))))
