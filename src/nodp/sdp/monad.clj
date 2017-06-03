(ns nodp.sdp.monad
  (:require [cats.builtin]
            [nodp.helpers :as helpers]))

(defn- multiply
  [& vs]
  (apply (helpers/lift-a *) vs))

(def prefix
  (partial str "The result is: "))

(def get-results
  (comp prefix
        multiply))

(get-results [1 2 3 4] [5 6 7 8])
