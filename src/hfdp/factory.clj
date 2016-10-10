(ns hfdp.factory
  (:require [clojure.walk :as walk]))

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

(defn quote-seq
  [x]
  (if (seq? x)
    `'~x
    (walk/walk quote-seq identity x)))

(defmacro functionize
  [operator]
  `(fn [& args#]
     (->> (walk/walk quote-seq identity args#)
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

(defn- log-operations
  [_]
  ;TODO possibly implement and use flip
  (doall (map println ["box" "cut" "bake" "prepare"])))

(def order
  (juxt log-operations log-pizza))

(order {:region :ny
        :kind   :cheese})
