(ns hfdp.iterator
  (:require [flatland.ordered.set :refer [ordered-set]]
            [hfdp.helpers :as helpers]))

(def pankake-menu
  (ordered-set {:name        "K&B's Pancake Breakfast"
                :description "Pancakes with scrambled eggs, and toast"
                :price       2.99}
               {:name        "Regular Pancake Breakfast"
                :description "Pancakes with fried eggs, sausage"
                :price       2.99}))

(def get-items
  (partial map identity))

(def print-menu
  (comp (partial helpers/printall)
        get-items))

(print-menu pankake-menu)
