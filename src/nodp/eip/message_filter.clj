(ns nodp.eip.message-filter
  (:require [clojure.string :as str]
            [clojurewerkz.money.amounts :as ma]
            [clojurewerkz.money.currencies :as mc]
            [nodp.helpers :as helpers]))

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

(def handle-items
  (comp (partial str "handling ")
        str/join))

(defn- make-handle-kind-items
  [kind]
  (fn [items]
    (if (->> items
            (filter (make-is-kind? kind))
            not-empty)
      (handle-items items))))

(def handle-a-items
  (make-handle-kind-items "ABC"))

(handle-a-items a-items)

(handle-a-items x-items)

(def handle-x-items
  (make-handle-kind-items "XYZ"))

(handle-x-items a-items)

(handle-x-items x-items)
