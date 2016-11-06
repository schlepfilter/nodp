(ns nodp.hfdp.factory
  (:require [clojure.string :as str]
            [nodp.helpers :as helpers]))

(defmulti get-regional-ingredient :region)

(defmethod get-regional-ingredient :ny
  [_]
  {:cheese    "Reggiano Cheese"
   :dough     "Thin Crust Dough"
   :clams     "Fresh Clams from Long Island Sound"
   :pepperoni "Sliced Pepperoni"                            ;
   :sauce     "Marinara Sauce"
   :vegies    #{"Garlic" "Onion" "Mashroom" "Red Pepper"}})

(defmulti get-kind-ingredients :kind)

(defmulti get-regional-name :region)

(defmethod get-regional-name :ny
  [_]
  "New York Style")

(defmulti get-kind-name :kind)

(helpers/defmethods :cheese
                    {get-kind-ingredients #{:dough :sauce :cheese}
                     get-kind-name        "Cheeze"})

(def get-pizza-name
  (comp
    (partial str/join " ")
    (partial (helpers/flip conj) "Pizza")
    (helpers/build vector get-regional-name get-kind-name)))

(def get-customer-pizza
  (helpers/build str
                 :customer
                 (constantly " ordered a ---- ")
                 get-pizza-name
                 (constantly " ----")))

(def get-ingredients
  (comp vals
        (helpers/build select-keys
                       get-regional-ingredient
                       get-kind-ingredients)))

(def prepare
  (comp (partial str "Preparing ")
        get-pizza-name))

(def constant-operations
  ["Bake for 25 minutes at 350"
   "Cutting the pizza into diagonal slices"
   "Place pizza in official PizzaStore box"])

(defn- wrap
  [x]
  (if (sequential? x)
    x
    [x]))

(defn- mix-concat-two
  [& more]
  (->> (map wrap more)
       (apply concat)))

(defn- mix-concat
  [& more]
  (reduce mix-concat-two more))

(def get-arguments
  (helpers/build mix-concat
                 prepare
                 (constantly constant-operations)
                 get-customer-pizza
                 get-ingredients))

(def order
  (comp helpers/printall
        get-arguments))

(order {:kind     :cheese
        :customer "Ethan"
        :region   :ny})
