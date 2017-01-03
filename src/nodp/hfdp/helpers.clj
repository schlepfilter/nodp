(ns nodp.hfdp.helpers
  (:require [flatland.ordered.set :refer [ordered-set]]
            [nodp.helpers :as helpers]))

(def pancake-menu-items
  (ordered-set
    {:title        "K&B's Pancake Breakfast"
     :description "Pancakes with scrambled eggs, and toast"
     :price       (helpers/get-usd 2.99)}
    {:title        "Regular Pancake Breakfast"
     :description "Pancakes with fried eggs, sausage"
     :price       (helpers/get-usd 2.99)}))

(def dinner-menu-items
  (ordered-set
    {:title        "Vegetarian BLT"
     :description "(Fakin') Bacon with lettuce & tomato on whole wheat"
     :price       (helpers/get-usd 2.99)}
    {:title        "BLT"
     :description "Bacon with lettuce & tomato on whole wheat"
     :price       (helpers/get-usd 2.99)}))
