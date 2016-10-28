(ns nodp.eip.message-filter.core
  (:require [clojurewerkz.money.amounts :as ma]
            [clojurewerkz.money.currencies :as mc]
            [clojure.string :as str]
            [nodp.helpers :as helpers]
            [nodp.eip.helpers :as eip-helpers]))

(def a-items
  [{:id    1
    :kind  "ABC.4"
    :price (ma/amount-of mc/USD 23.95)}
   {:id    2
    :kind  "ABC.1"
    :price (ma/amount-of mc/USD 99.95)}
   {:id    3
    :kind  "ABC.9"
    :price (ma/amount-of mc/USD 14.95)}])

(def x-items
  [{:id    4
    :kind  "XYZ.2"
    :price (ma/amount-of mc/USD 74.95)}
   {:id    5
    :kind  "XYZ.1"
    :price (ma/amount-of mc/USD 59.95)}
   {:id    6
    :kind  "XYZ.7"
    :price (ma/amount-of mc/USD 29.95)}
   {:id    7
    :kind  "XYZ.7"
    :price (ma/amount-of mc/USD 9.95)}])

(defn- make-is-kind?
  [s]
  (comp (partial (helpers/flip str/starts-with?) s)
        :kind))

(defn- make-filter-items
  [kind]
  (partial filter (make-is-kind? kind)))

(defn- make-handle-kind-items
  [kind]
  (comp eip-helpers/handle-items
        (make-filter-items kind)))

((make-handle-kind-items "ABC") a-items)

((make-handle-kind-items "ABC") x-items)
