(ns nodp.hfdp.iterator
  (:require [clojurewerkz.money.amounts :as ma]
            [clojurewerkz.money.currencies :as mc]
            [clojurewerkz.money.format :as format]
            [flatland.ordered.set :refer [ordered-set]]
            [nodp.helpers :as helpers]))

(def pancake-menu
  (ordered-set
    {:name        "K&B's Pancake Breakfast"
     :description "Pancakes with scrambled eggs, and toast"
     :price       (ma/amount-of mc/USD 2.99)}
    {:name        "Regular Pancake Breakfast"
     :description "Pancakes with fried eggs, sausage"
     :price       (ma/amount-of mc/USD 2.99)}))

(def dinner-menu
  (ordered-set
    {:name        "Vegetarian BLT"
     :description "(Fakin') Bacon with lettuce & tomato on whole wheat"
     :price       (ma/amount-of mc/USD 2.99)}
    {:name        "BLT"
     :description "Bacon with lettuce & tomato on whole wheat"
     :price       (ma/amount-of mc/USD 2.99)}))

(def get-item
  (helpers/build str
                 :name
                 (constantly ", ")
                 (comp format/format :price)
                 (constantly " -- ")
                 :description))

(def get-items
  (partial map get-item))

(def print-menus
  (comp (partial helpers/printall)
        get-items
        concat))

(print-menus pancake-menu dinner-menu)
