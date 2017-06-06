(ns nodp.hfdp.abstract-factory
  (:require [aid.core :as help]
            [nodp.helpers :as helpers]))

(helpers/defmultis-identity get-kind-ingredients
                            get-kind-name
                            get-regional-ingredient
                            get-regional-name)

(helpers/defmethods :ny
                    {get-regional-ingredient {:cheese    "Reggiano Cheese"
                                              :dough     "Thin Crust Dough"
                                              :clams     "Fresh Clams from Long Island Sound"
                                              :pepperoni "Sliced Pepperoni" ;
                                              :sauce     "Marinara Sauce"
                                              :vegies    #{"Garlic"
                                                           "Onion"
                                                           "Mashroom"
                                                           "Red Pepper"}}
                     get-regional-name       "New York Style"})

(helpers/defmethods :cheese
                    {get-kind-ingredients #{:dough :sauce :cheese}
                     get-kind-name        "Cheeze"})

(def space-join-juxt
  (comp (partial comp helpers/space-join)
        juxt))

(def get-pizza-name
  (space-join-juxt (comp get-regional-name
                         :region)
                   (comp get-kind-name
                         :kind)
                   (constantly "Pizza")))

(def get-customer-pizza
  (space-join-juxt :customer
                   (constantly "ordered a ----")
                   get-pizza-name
                   (constantly "----")))

(def get-ingredients
  (comp vals
        (help/build select-keys
                    (comp get-regional-ingredient
                          :region)
                    (comp get-kind-ingredients
                          :kind))))

(def prepare
  (comp (partial str "Preparing ")
        get-pizza-name))

(def constant-operations
  ["Bake for 25 minutes at 350"
   "Cutting the pizza into diagonal slices"
   "Place pizza in official PizzaStore box"])

(def order
  (comp flatten
        (juxt prepare
              (constantly constant-operations)
              get-customer-pizza
              get-ingredients)))

(order {:kind     :cheese
        :customer "Ethan"
        :region   :ny})
