(ns nodp.jdp.half-sync-half-async
  (:require [nodp.helpers :as helpers]))

(defn- sum-arithmetic
  [i]
  (* i (inc i)))

;This definition is less readable.
;(def arithmatic-sum
;  (comp
;    (partial (helpers/flip /) 2)
;    (helpers/build * identity inc)))

(def print-sum
  (juxt (helpers/functionize Thread/sleep)
        (comp println sum-arithmetic)))

(defn- future-pmap
  [f coll]
  (-> (pmap f coll)
      future))

;This is more literal translation of the original Java code.
;(defmacro do-future
;  [f x]
;  `(future (~f ~x)))
;
;(defn do-map-future
;  [f coll]
;  (when (not-empty coll)
;    (do-future f (first coll))
;    (do-map-future f (rest coll))))

(future-pmap print-sum [1000 500 2000 1])
