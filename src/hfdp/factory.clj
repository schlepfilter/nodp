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

(def get-pizza
  (juxt get-ingredients get-pizza-name))

(def operations
  ["box" "cut" "bake" "prepare"])

(def get-arguments
  (comp (partial (helpers/functionize lazy-cat) operations)
        get-pizza))

(def order
  (comp helpers/printall
        get-arguments))

(order {:region :ny
        :kind   :cheese})
