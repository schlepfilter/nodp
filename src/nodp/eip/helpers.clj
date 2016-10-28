(ns nodp.eip.helpers
  (:require [clojure.string :as str]))

(def handle-items
  (comp (partial str "handling ")
        str/join))


