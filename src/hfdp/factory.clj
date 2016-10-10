(ns hfdp.factory
  (:require [riddley.walk :as riddley]))

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

(defn quote-form
  [form]
  `'~form)

(def quote-seq
  (partial riddley/walk-exprs seq? quote-form))

(defmacro functionize
  [operator]
  `(fn [& args#]
     (->> (map quote-seq args#)
          (cons '~operator)
          eval)))

(defmacro build
  [operator & fs]
  `(comp
     (partial apply (functionize ~operator))
     (juxt ~@fs)))

(def log-pizza
  ;TODO log the name of the pizza
  (comp (partial println)
        (build select-keys
               get-regional-ingredient
               get-kind-ingredients)))

(def operations
  ["box" "cut" "bake" "prepare"])

(defn- log-operations
  [_]
  (dorun (map println operations)))

(def order
  (juxt log-operations log-pizza))

(order {:region :ny
        :kind   :cheese})
