(ns nodp.hfdp.factory
  (:require [clojure.string :as str]
            [nodp.helpers :as helpers]))

(helpers/defmultis-identity #{'get-kind-ingredients
                              'get-kind-name
                              'get-regional-ingredient
                              'get-regional-name})

(defmethod get-regional-ingredient :ny
  [_]
  {:cheese    "Reggiano Cheese"
   :dough     "Thin Crust Dough"
   :clams     "Fresh Clams from Long Island Sound"
   :pepperoni "Sliced Pepperoni"                            ;
   :sauce     "Marinara Sauce"
   :vegies    #{"Garlic" "Onion" "Mashroom" "Red Pepper"}})

(defmethod get-regional-name :ny
  [_]
  "New York Style")

(helpers/defmethods :cheese
                    {get-kind-ingredients #{:dough :sauce :cheese}
                     get-kind-name        "Cheeze"})

(def get-pizza-name
  (comp
    (partial str/join " ")
    (partial (helpers/flip conj) "Pizza")
    (helpers/build vector
                   (comp get-regional-name
                         :region)
                   (comp get-kind-name
                         :kind))))

(def get-customer-pizza
  (helpers/build str
                 :customer
                 (constantly " ordered a ---- ")
                 get-pizza-name
                 (constantly " ----")))

(def get-ingredients
  (comp vals
        (helpers/build select-keys
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
