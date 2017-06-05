(ns nodp.sdp.partial-function
  (:require [cats.core :as m]
            [cats.monad.maybe :as maybe]
            [clojure.math.numeric-tower :as math]
            [help.core :as help]))

(defn- sqrt
  [x]
  (help/maybe-if-not (neg? x)
                     (math/sqrt x)))

(defn- get-sqrt-defined
  [x]
  (str "Can we calculate a root for " x ": " (-> x
                                                 sqrt
                                                 maybe/just?)))

;This definition is less readable.
;(def get-sqrt-defined
;  (helpers/build str
;                 (constantly "Can we calculate a root for ")
;                 identity
;                 (constantly ": ")
;                 (comp maybe/just? sqrt)))

(get-sqrt-defined -10)

(def get-sqrts
  (comp (partial str "Square roots: ")
        vec
        (partial maybe/map-maybe sqrt)))

(def items
  [-1 10 11 -36 36 -49 49 81])

(get-sqrts items)

(defmacro join-or-else
  [then else]
  `(fn [x#]
     (let [then-result# (~then x#)]
       (help/casep then-result#
                   maybe/just? (m/join then-result#)
                   (~else x#)))))

(def square
  (partial (help/flip math/expt) 2))

(def get-sqrt-squares
  (comp (partial str "Square roots or squares: ")
        vec
        (partial map (join-or-else sqrt square))))

(get-sqrt-squares items)
