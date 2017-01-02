(ns nodp.hfdp.composite
  (:require [clojurewerkz.money.format :as format]
            [com.rpl.specter :as s]
            [flatland.ordered.set :refer [ordered-set]]
            [nodp.helpers :as helpers]
            [nodp.hfdp.helpers :as hfdp-helpers]))

(def pancake-menu
  (ordered-set {:title       "PANCAKE HOUSE MENU"
                :description "Breakfast"
                :items       hfdp-helpers/pancake-menu-items}))

(def desert-menu-items
  (ordered-set {:title       "Apple Pie"
                :description "Apple pie with a flakey crust, topped with vanilla icecream"
                :price       (helpers/get-usd 1.59)}
               {:title       "Cheesecake"
                :description "Creamy New York cheesecake, with a chocolate graham crust"
                :price       (helpers/get-usd 1.99)}))

(def desert-menu
  (ordered-set {:title       "DESERT MENU"
                :description "Dessert of course!"
                :items       desert-menu-items}))

(def dinner-menu
  (ordered-set {:title       "DINNER MENU"
                :description "Lunch"
                :items       hfdp-helpers/dinner-menu-items}
               desert-menu))

(def all-menu
  (ordered-set {:title       "ALL MENUS"
                :description "All menus combined"
                :items       (ordered-set)} pancake-menu dinner-menu))

(def Menus
  (s/recursive-path [] p
                    (s/cond-path set? [s/ALL p]
                                 map? s/STAY)))

(def get-item
  (helpers/build str
                 :title
                 (constantly "(v), ")
                 (comp format/format
                       :price)
                 (constantly "\n -- ")
                 :description))

(def get-items
  (comp (partial apply str) (partial map get-item)))

(def get-menu
  (helpers/build str
                 :title
                 (constantly ", ")
                 :description
                 (constantly "\n---------------------\n")
                 (comp get-items
                       :items)))

(def get-menus
  (comp (partial map get-menu)
        (partial s/select* Menus)))

(get-menus all-menu)
