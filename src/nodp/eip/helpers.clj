(ns nodp.eip.helpers
  (:require [clojure.string :as str]
            [nodp.helpers :as helpers]))

(def a-items
  [{:id    1
    :kind  "ABC.4"
    :price (helpers/get-usd 23.95)}
   {:id    2
    :kind  "ABC.1"
    :price (helpers/get-usd 99.95)}
   {:id    3
    :kind  "ABC.9"
    :price (helpers/get-usd 14.95)}])

(def x-items
  [{:id    4
    :kind  "XYZ.2"
    :price (helpers/get-usd 74.95)}
   {:id    5
    :kind  "XYZ.1"
    :price (helpers/get-usd 59.95)}
   {:id    6
    :kind  "XYZ.7"
    :price (helpers/get-usd 29.95)}
   {:id    7
    :kind  "XYZ.7"
    :price (helpers/get-usd 9.95)}])

(def handle-items
  (comp (partial str "handling ")
        str/join))
