(ns nodp.test.helpers
  (:require [clojure.test.check.generators :as gen]
            [clojure.test.check.random :as random]
            [clojure.test.check.rose-tree :as rose]))

(defn generate
  ([generator {:keys [seed size]
               :or   {size 30}}]
   (let [rng (if seed
               (random/make-random seed)
               (random/make-random))]
     (rose/root (gen/call-gen generator rng size)))))

(def function
  (gen/fmap (fn [n]
              (memoize (fn [x]
                         (generate gen/any {:seed (+ n (hash x))}))))
            gen/int))
