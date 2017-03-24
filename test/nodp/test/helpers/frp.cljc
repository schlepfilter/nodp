(ns nodp.test.helpers.frp
  (:require [clojure.test.check]
            [clojure.test.check.clojure-test
             :as clojure-test
             :include-macros true]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop :include-macros true]
            [cats.monad.maybe :as maybe]
            [#?(:clj  clojure.core.async
                :cljs cljs.core.async) :as async]
            [nodp.helpers :as helpers]
            [nodp.helpers.frp :as frp]
            [nodp.helpers.primitives.event :as event]
            [nodp.helpers.tuple :as tuple]
            [#?(:clj  clojure.test
                :cljs cljs.test) :as test :include-macros true]
    #?(:clj
            [riddley.walk :as walk]))
  #?(:cljs (:require-macros [nodp.test.helpers.frp :refer [with-result]])))

(defn fixture
  [f]
  (frp/restart)
  (with-redefs [event/queue helpers/funcall]
    (f)))

(test/use-fixtures :each fixture)

(clojure-test/defspec
  invoke-inactive
  10
  (prop/for-all [a gen/any]
                (let [e (frp/event)]
                  (e a)
                  (maybe/nothing? @e))))

(clojure-test/defspec
  invoke-active
  10
  (prop/for-all [as (gen/vector gen/any)]
                (let [e (frp/event)]
                  (frp/activate)
                  (run! e as)
                  (= (tuple/snd @@e) (last as)))))

#?(:clj (defmacro with-result
          [result-name & body]
          (potemkin/unify-gensyms
            `(let [result-state## (atom helpers/nothing)]
               ~(walk/walk-exprs
                  (partial = result-name)
                  (fn [_#]
                    `(comp (partial reset! result-state##)
                           maybe/just))
                  (cons 'do body))
               @@result-state##))))

(clojure-test/defspec
  with-result-identity
  10
  (prop/for-all [a gen/any
                 b gen/any]
                (= (with-result result
                                (result a)
                                b)
                   a)))

(clojure-test/defspec
  on-identity
  10
  (prop/for-all [a gen/any]
                (= (with-result result
                                (let [e (frp/event)]
                                  (frp/on result e)
                                  (frp/activate)
                                  (e a)))
                   a)))
