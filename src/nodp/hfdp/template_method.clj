(ns nodp.hfdp.template-method
  (:require [help :as help]
            [nodp.helpers :as helpers]))

(helpers/defmultis-identity brew add-condiments)

(helpers/defmethods :coffee
                    {brew           "Dripping Coffee through filter"
                     add-condiments "Adding Sugar and Milk"})

(helpers/defmethods :tea
                 {brew           "Steeping the tea"
                  add-condiments "Adding Lemon"})

(def prepare
  (help/build vector
              (constantly "Boiling water")
              brew
              (constantly "Pouring into cup")
              add-condiments))

(prepare :coffee)

(prepare :tea)
