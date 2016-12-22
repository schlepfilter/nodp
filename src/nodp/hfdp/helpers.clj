(ns nodp.hfdp.helpers
  (:require [clojurewerkz.money.amounts :as ma]
            [clojurewerkz.money.currencies :as mc]
            [flatland.ordered.set :refer [ordered-set]]))

(def pancake-menu-items
  (ordered-set
    {:title        "K&B's Pancake Breakfast"
     :description "Pancakes with scrambled eggs, and toast"
     :price       (ma/amount-of mc/USD 2.99)}
    {:title        "Regular Pancake Breakfast"
     :description "Pancakes with fried eggs, sausage"
     :price       (ma/amount-of mc/USD 2.99)}))

(def dinner-menu-items
  (ordered-set
    {:title        "Vegetarian BLT"
     :description "(Fakin') Bacon with lettuce & tomato on whole wheat"
     :price       (ma/amount-of mc/USD 2.99)}
    {:title        "BLT"
     :description "Bacon with lettuce & tomato on whole wheat"
     :price       (ma/amount-of mc/USD 2.99)}))

