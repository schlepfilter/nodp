(ns nodp.sdp.monad
  (:require [cats.builtin]
            [help.core :as help]))

(def prefix
  (partial str "The result is: "))

(def get-results
  (comp prefix
        (help/lift-a *)))

(get-results [1 2 3 4] [5 6 7 8])
