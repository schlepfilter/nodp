(ns nodp.test.helpers.frp
  (:require [clojure.test.check]
            [clojure.test.check.clojure-test
             :as clojure-test
             :include-macros true]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop :include-macros true]
            [cats.monad.maybe :as maybe]
            [nodp.helpers.frp :as frp]
            [nodp.helpers :as helpers]
    #?@(:clj  [
            [clojure.test :as test]
            [riddley.walk :as walk]]
        :cljs [[cljs.test :as test :include-macros true]]))
  #?(:cljs (:require-macros [nodp.test.helpers.frp :refer [with-result]])))

(defn fixture
  [f]
  (frp/initialize)
  (f))

(test/use-fixtures :each fixture)

(defn promise-or-atom
  []
  #?(:clj  (promise)
     :cljs (atom helpers/nothing)))

(def deliver-or-reset!
  #?(:clj  deliver
     :cljs reset!))

#?(:clj (defmacro with-result
          [result-name expr]
          (potemkin/unify-gensyms
            `(let [result-state## (promise-or-atom)]
               ~(walk/walk-exprs
                  (partial = result-name)
                  (fn [_#]
                    `(comp (partial deliver-or-reset! result-state##)
                           maybe/just)) expr)
               @@result-state##))))

(clojure-test/defspec
  with-result-identity
  10
  (prop/for-all [a gen/any
                 b gen/any]
                (= (with-result result
                                (do (result a)
                                    b))
                   a)))
