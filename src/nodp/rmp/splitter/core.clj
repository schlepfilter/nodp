(ns nodp.rmp.splitter.core
  (:require [nodp.helpers :as helpers]))

(def items
  [{:id    1
    :kind  "A"
    :price (helpers/get-usd 23.95)}
   {:id    2
    :kind  "B"
    :price (helpers/get-usd 99.95)}
   {:id    3
    :kind  "C"
    :price (helpers/get-usd 14.95)}])

(def handle-item
  (partial str "handling "))

(map handle-item items)
