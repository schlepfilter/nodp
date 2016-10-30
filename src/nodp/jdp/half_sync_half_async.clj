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

(pmap print-sum [1000 500 2000 1])
