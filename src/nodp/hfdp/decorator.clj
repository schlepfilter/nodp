(ns nodp.hfdp.decorator
  (:require [nodp.helpers :as helpers]))

(def dark-roast
  {:name  "Dark Roast Coffee"
   :price 0.99})

(def mocha
  {:name  "Mocha"
   :price 0.2})

(def whip
  {:name  "Whip"
   :price 0.1})

(defmulti add (comp type
                    first
                    vector))

(helpers/defpfmethod add String
                     (comp helpers/comma-join
                           vector))

(helpers/defpfmethod add Number
                     +)

(merge-with add dark-roast mocha mocha whip)
