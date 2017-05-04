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

(def probability
  (gen/double* {:max  1
                :min  0
                :NaN? false}))

(def event
  ;gen/fmap ensures a new event is returned
  ;(gen/sample (gen/return (rand)) 2)
  ;=> (0.7306051862977597 0.7306051862977597)
  ;(gen/sample (gen/fmap (fn [_] (rand))
  ;                      (gen/return 0))
  ;            2)
  ;=> (0.8163040448517938 0.8830449199816961)
  (gen/let [a any-equal]
           (gen/one-of [(gen/return (frp/event))
                        (gen/return (nodp.helpers/pure
                                      (helpers/infer (frp/event))
                                      a))])))

(defn conj-event
  [coll probability*]
  (->> coll
       count
       inc
       (* probability*)
       int
       (if (= 1.0 probability*)
         0)
       (nth (conj coll
                  (generate event {:seed (hash probability*)})))
       (conj coll)))

(def get-events
  (partial reduce conj-event []))

(def advance
  (gen/let [n gen/pos-int]
           (let [e (frp/event)]
             (fn []
               (dotimes [_ n]
                 (e unit/unit))))))
