(ns nodp.eip.message-filter
  (:require [clojurewerkz.money.amounts :as ma]
            [clojurewerkz.money.currencies :as mc]
            [clojure.string :as str]
            [nodp.helpers :as helpers]
            [cats.core :as m]))

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
  [kind]
  (comp (partial (helpers/flip str/starts-with?) kind)
        :kind))

(def make-filter-items
  (comp (m/curry 2 filter)
        make-is-kind?))

(defn handle-items
  [items]
  (if (not-empty items)
    (->> items
         str/join
         (str "handling "))))

(def make-handle-kind-items
  (comp (partial comp handle-items)
        make-filter-items))

((make-handle-kind-items "ABC") a-items)

((make-handle-kind-items "ABC") x-items)
