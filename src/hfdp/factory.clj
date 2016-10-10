(ns hfdp.factory
  (:require [riddley.walk :as riddley]
            [clojure.string :as str]))

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

(defn flip
  [f]
  (fn
    ([] (f))
    ([x] (f x))
    ([x y & more] (apply f y x more))))

(def get-pizza-name
  (comp
    (partial (flip str) " Pizza")
    (partial str/join " ")
    (build vector get-regional-name get-kind-name)))

(def get-ingredients
  (build select-keys get-regional-ingredient get-kind-ingredients))

(def log-pizza
  (juxt
    (comp println get-ingredients)
    (comp println get-pizza-name)))

(def operations
  ["box" "cut" "bake" "prepare"])

(defn- log-operations
  []
  (-> (map println operations)
      dorun))

(defn- order
  [pizza]
  (log-operations)
  (log-pizza pizza))

(order {:region :ny
        :kind   :cheese})
