(ns nodp.jdp.half-sync-half-async
  (:require [cats.core :as m]
            [nodp.helpers :as helpers]))

(def sum-arithmetic
  (m/<*> (helpers/curry *) inc))

(def print-sum
  (juxt (helpers/functionize Thread/sleep)
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
