(ns nodp.sdp.monad
  (:require [cats.builtin]
            [nodp.helpers :as helpers]))

(def prefix
  (partial str "The result is: "))

(def get-results
  (comp prefix
        (helpers/lift-a *)))

(get-results [1 2 3 4] [5 6 7 8])
