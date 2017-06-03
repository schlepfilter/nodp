(ns nodp.jdp.half-sync-half-async
  (:require [cats.core :as m]
            [help]))

(def sum-arithmetic
  (m/<*> (help/curry *) inc))

(def print-sum
  (juxt (help/functionize Thread/sleep)
        (comp println
              sum-arithmetic)))

(defn- future-pmap
  [& more]
  (-> (apply pmap more)
      future))

;This is more literal translation of the original Java code.
;(defn- do-future
;  [f x]
;  (future (f x)))
;
;(defn do-map-future
;  [f coll]
;  (when (not-empty coll)
;    (do-future f (first coll))
;    (do-map-future f (rest coll))))

(future-pmap print-sum [1000 500 2000 1])
