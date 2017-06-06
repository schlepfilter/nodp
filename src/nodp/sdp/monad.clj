(ns nodp.sdp.monad
  (:require [aid.core :as aid]
            [cats.builtin]))

(def prefix
  (partial str "The result is: "))

(def get-results
  (comp prefix
        (aid/lift-a *)))

(get-results [1 2 3 4] [5 6 7 8])
