(ns nodp.hfdp.decorator
  (:require [help]
            [nodp.helpers :as helpers]))

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

(help/defpfmethod add String
                  (comp helpers/comma-join
                        vector))

(help/defpfmethod add Number
                  +)

(merge-with add dark-roast mocha mocha whip)
