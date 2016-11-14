(ns nodp.sdp.monad
  (:require [nodp.helpers :as helpers]))

(defn- multiply
  [& vs]
  (apply helpers/ap * vs))

(multiply [1 2 3 4] [5 6 7 8])
