(ns nodp.test.helpers.frp
  (:require [clojure.test.check.clojure-test :as clojure-test :include-macros true]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop :include-macros true]
            [nodp.helpers.frp :as frp]
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
     :cljs (atom false)))

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
                    `(partial deliver-or-reset! result-state##)) expr)
               @result-state##))))

(test/deftest with-result-true
  (test/is (with-result result
                        (do (result true)
                            false))))
