(ns nodp.test.helpers.tuple
  (:require [cats.builtin]
            [cats.monad.maybe :as maybe]
    ;clojure.test.check is required to avoid the following warning.
    ;Figwheel: Watching build - test
    ;Figwheel: Cleaning build - test
    ;Compiling "resources/public/test/js/main.js" from ["src" "test"]...
    ;WARNING: Use of undeclared Var clojure.test.check/quick-check
            [clojure.test.check]
            [clojure.test.check.clojure-test :as clojure-test :include-macros true]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop :include-macros true]
            [nodp.helpers :as helpers]
            [nodp.helpers.tuple :as tuple]
            [nodp.helpers.unit :as unit]
            [nodp.test.helpers :as test-helpers]))

(defn maybe
  [generator]
  (gen/bind generator
            (fn [a]
              (gen/one-of [(gen/return helpers/nothing)
                           (gen/return (maybe/just a))]))))

(def scalar-monoids
  [gen/string
   (gen/return unit/unit)
   (gen/vector test-helpers/any-equal)
   (gen/list test-helpers/any-equal)
   (gen/set test-helpers/any-equal)
   (gen/map test-helpers/any-equal test-helpers/any-equal)])

(def scalar-monoid
  (gen/one-of scalar-monoids))

(def monoid
  (gen/one-of [scalar-monoid
               (gen/recursive-gen maybe scalar-monoid)]))

(def mempty
  (gen/fmap (comp nodp.helpers/mempty helpers/infer)
            monoid))

(defn scalar-monoid-vector
  [n]
  (gen/one-of (map (partial (helpers/flip gen/vector) n)
                   scalar-monoids)))

(clojure-test/defspec
  monad-right-identity-law
  10
  (prop/for-all [a test-helpers/any-equal
                 mempty* mempty]
                (= (nodp.helpers/>>= (tuple/tuple mempty* a) nodp.helpers/return)
                   (tuple/tuple mempty* a))))

(clojure-test/defspec
  monad-left-identity-law
  10
  (prop/for-all [a test-helpers/any-equal
                 f* (test-helpers/function test-helpers/any-equal)
                 monoid* monoid]
                (let [f (comp (partial tuple/tuple monoid*)
                              f*)]
                  (= (nodp.helpers/>>= (tuple/tuple (-> monoid*
                                                        helpers/infer
                                                        nodp.helpers/mempty)
                                                    a)
                                       f)
                     (f a)))))

(clojure-test/defspec
  monad-associativity-law
  10
  (prop/for-all [a test-helpers/any-equal
                 monoids (scalar-monoid-vector 3)
                 f* (test-helpers/function test-helpers/any-equal)
                 g* (test-helpers/function test-helpers/any-equal)]
                (let [f (comp (partial tuple/tuple (second monoids))
                              f*)
                      g (comp (partial tuple/tuple (last monoids))
                              g*)
                      ma (tuple/tuple (first monoids) a)]
                  (= (nodp.helpers/->= ma f g)
                     (nodp.helpers/>>= ma (comp (partial nodp.helpers/=<< g)
                                                f))))))
