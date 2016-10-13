(ns hfdp.iterator
  (:require [flatland.ordered.set :refer [ordered-set]]))

(def pankake-menu
  (ordered-set {:name        "K&B's Pancake Breakfast"
                :description "Pancakes with scrambled eggs, and toast"
                :price       2.99}
               {:name        "Regular Pancake Breakfast"
                :description "Pancakes with fried eggs, sausage"
                :price       2.99}))

(def print-menu
  (partial dorun (map println pankake-menu)))

(print-menu)
