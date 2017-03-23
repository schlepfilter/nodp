(ns nodp.test.helpers.frp
  (:require [clojure.test.check.clojure-test :as clojure-test :include-macros true]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop :include-macros true]
            [nodp.helpers.frp :as frp]
    #?@(:clj  [
            [clojure.test :as test]
            [riddley.walk :as walk]]
        :cljs [[cljs.test :as test :include-macros true]]))
  #?(:cljs (:require-macros [nodp.test.helpers.frp :refer [with-return]])))

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

#?(:clj (defmacro with-return
          [return-name expr]
          (potemkin/unify-gensyms
            `(let [result-state## (promise-or-atom)]
               ~(walk/walk-exprs
                  (partial = return-name)
                  (fn [_#]
                    `(partial deliver-or-reset! result-state##)) expr)
               @result-state##))))

(test/deftest with-return-true
  (test/is (with-return return
                        (do (return true)
                            false))))
