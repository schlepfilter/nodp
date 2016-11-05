(ns nodp.hfdp.template-method
  (:require [nodp.helpers :as helpers]))

(helpers/defmultis-identity #{'brew 'add-condiments})

(helpers/defmethods :coffee
                    {brew           "Dripping Coffee through filter"
                     add-condiments "Adding Sugar and Milk"})

(helpers/defmethods :tea
                    {brew           "Steeping the tea"
                     add-condiments "Adding Lemon"})

(defn prepare
  [kind]
  ["Boiling water"
   (brew kind)
   "Pouring into cup"
   (add-condiments kind)])

(helpers/printall (prepare :coffee))

(helpers/printall (prepare :tea))
