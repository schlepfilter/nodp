(ns nodp.test.helpers
  (:require [clojure.test.check.generators :as gen :include-macros true]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.random :as random]
            [clojure.test.check.rose-tree :as rose]
    #?(:clj
            [clojure.math.numeric-tower :as numeric-tower])
            [nodp.helpers :as helpers]
            [nodp.helpers.unit :as unit]
            [nodp.helpers.frp :as frp]))

(defn fixture
  [f]
  (reset! helpers/network-state (helpers/get-initial-network))
  ;TODO redefine event/queue
  (f))

(def num-tests
  #?(:clj  10
     :cljs 2))

(def restart
  ;TODO call restart
  (gen/fmap (fn [_]
              (frp/restart))
            (gen/return unit/unit)))

#?(:clj (defmacro restart-for-all
          [bindings & body]
          ;TODO generate times and redefine get-new-time
          `(prop/for-all ~(concat `[_# restart]
                                  bindings)
                         ~@body)))

(defn generate
  ([generator {:keys [seed size]
               :or   {size 30}}]
   (let [rng (if seed
               (random/make-random seed)
               (random/make-random))]
     (rose/root (gen/call-gen generator rng size)))))

(defn function
  [generator]
  (gen/fmap (fn [n]
              (memoize (fn [& more]
                         (generate generator {:seed (+ n (hash more))}))))
            gen/int))

(def simple-type-equal
  (gen/one-of [gen/int
               gen/large-integer
               (gen/double* {:NaN? false})
               gen/char
               gen/string
               gen/ratio
               gen/boolean
               gen/keyword
               gen/keyword-ns
               gen/symbol
               gen/symbol-ns
               gen/uuid]))

(def any-equal
  (gen/recursive-gen gen/container-type simple-type-equal))

(def advance
  (gen/let [n gen/pos-int]
           (let [e (frp/event)]
             (fn []
               (dotimes [_ n]
                 (e unit/unit))))))
