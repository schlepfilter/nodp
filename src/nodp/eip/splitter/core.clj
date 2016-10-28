(ns nodp.eip.splitter.core
  (:require [clojurewerkz.money.amounts :as ma]
            [clojurewerkz.money.currencies :as mc]))

(def items
  [{:id    1
    :kind  "A"
    :price (ma/amount-of mc/USD 23.95)}
   {:id    2
    :kind  "B"
    :price (ma/amount-of mc/USD 99.95)}
   {:id    3
    :kind  "C"
    :price (ma/amount-of mc/USD 14.95)}])

(def handle-item
  (partial str "handling "))

(map handle-item items)
