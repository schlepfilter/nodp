(ns nodp.sdp.partial-function
  (:require [cats.monad.maybe :as maybe]
            [clojure.math.numeric-tower :as math]
            [nodp.helpers :as helpers]))

(def square
  (partial (helpers/flip math/expt) 2))

(defn- sqrt
  [x]
  (helpers/maybe-not (neg? x)
                     (math/sqrt x)))

(defn- get-sqrt-defined
  [x]
  (str "Can we calculate a root for " x ": " (-> x
                                                 sqrt
                                                 maybe/just?)))

;This definition is less readable
;(def get-sqrt-defined
;  (helpers/build str
;                 (constantly "Can we calculate a root for ")
;                 identity
;                 (constantly ": ")
;                 (comp maybe/just? sqrt)))

(get-sqrt-defined -10)
