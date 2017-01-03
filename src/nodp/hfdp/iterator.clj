(ns nodp.hfdp.iterator
  (:require [clojurewerkz.money.format :as format]
            [flatland.ordered.set :refer [ordered-set]]
            [nodp.helpers :as helpers]
            [nodp.hfdp.helpers :as hfdp-helpers]))

(def get-item
  (helpers/build str
                 :title
                 (constantly ", ")
                 (comp format/format
                       :price)
                 (constantly " -- ")
                 :description))

(def get-items
  (comp (partial map get-item)
        concat))

(get-items hfdp-helpers/pancake-menu-items hfdp-helpers/dinner-menu-items)
