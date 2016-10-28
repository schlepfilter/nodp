(ns nodp.eip.splitter.core
  (:require [clojurewerkz.money.amounts :as ma]
            [clojurewerkz.money.currencies :as mc]
            [nodp.eip.helpers :as eip-helpers]))

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

(eip-helpers/handle-items items)
