(ns nodp.test.helpers
  (:require [clojure.test.check.generators :as gen :include-macros true]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.random :as random]
            [clojure.test.check.rose-tree :as rose]
    #?(:clj
            [clojure.math.numeric-tower :as numeric-tower])
            [nodp.helpers :as helpers]
            [nodp.helpers.frp :as frp]
            [nodp.helpers.primitives.event :as event]
            [nodp.helpers.unit :as unit]))

(defn fixture
  [f]
  (reset! event/network-state (event/get-initial-network))
  ;TODO redefine event/queue
  ;TODO redefine get-new-time
  (f))

(def cljc-num-tests
  #?(:clj  10
     :cljs 2))

(def cljs-num-tests
  10)

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

(defn probabilities
  [n]
  (gen/sized (comp (partial gen/vector
                            probability
                            n)
                   (partial + n))))

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

(defn make-iterate
  [coll]
  (let [state (atom coll)]
    (memoize (fn [& _]
               (let [result (first @state)]
                 (swap! state rest)
                 result)))))

(defn events-tuple
  [probabilities]
  (gen/let [input-events (gen/return (get-events probabilities))
            fs (gen/vector (function any-equal)
                           (count input-events))]

           (gen/tuple (gen/return input-events)
                      (gen/return (doall (map helpers/<$> fs input-events))))))

(def advance
  (gen/let [n gen/pos-int]
           (let [e (frp/event)]
             (fn []
               (dotimes [_ n]
                 (e unit/unit))))))
