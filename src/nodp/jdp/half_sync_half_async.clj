(ns nodp.jdp.half-sync-half-async)

(defn- sum-arithmetic
  [i]
  (* i (inc i)))

;This definition is less readable.
;(def arithmatic-sum
;  (comp
;    (partial (helpers/flip /) 2)
;    (helpers/build * identity inc)))

(defn- print-sum
  [i]
  (Thread/sleep i)
  (-> i
      sum-arithmetic
      println))

(defmacro future-pmap
  [f coll]
  `(future (pmap ~f ~coll)))

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
