(ns nodp.test.helpers.tuple
  (:require [clojure.test.check]
            [clojure.test.check.clojure-test :as clojure-test :include-macros true]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [nodp.helpers.unit :as unit]))

(def function
  (gen/fmap (fn [_]
              (memoize (fn [_]
                         (gen/generate gen/any))))
            (gen/return unit/unit)))

(clojure-test/defspec tuple-monad-left-identity-law
                      10
                      (prop/for-all [a gen/any
                                     f function]
                                    (= (f a)
                                       (f a))))