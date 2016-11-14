(ns nodp.sdp.monad
  (:require [nodp.helpers :as helpers]))

(defn- multiply
  [& vs]
  (apply helpers/ap * vs))

(def prefix
  (partial str "The result is: "))

(def get-arguments
  (comp prefix
        multiply))

(def print-result
  (comp println
        get-arguments))

(print-result [1 2 3 4] [5 6 7 8])
