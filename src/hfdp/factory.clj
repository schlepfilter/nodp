(ns hfdp.factory
  (:require [clojure.string :as str]
            [hfdp.helpers :as helpers]))

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

(defmethod get-kind-ingredients :cheese
  [_]
  #{:dough :sauce :cheese})

(defmulti get-regional-name :region)

(defmethod get-regional-name :ny
  [_]
  "New York Style")

(defmulti get-kind-name :kind)

(defmethod get-kind-name :cheese
  [_]
  "Cheeze")

(def get-pizza-name
  (comp
    (partial str/join " ")
    (partial (helpers/flip conj) "Pizza")
    (helpers/build vector get-regional-name get-kind-name)))

(def get-ingredients
  (helpers/build select-keys get-regional-ingredient get-kind-ingredients))

(def log-pizza
  (helpers/build println get-ingredients get-pizza-name))

(def operations
  ["box" "cut" "bake" "prepare"])

(defn- log-operations
  []
  (helpers/printall operations))

(defn- order
  [pizza]
  (log-operations)
  (log-pizza pizza))

(order {:region :ny
        :kind   :cheese})
