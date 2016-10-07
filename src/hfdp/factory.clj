(ns hfdp.factory)

(defmulti get-regional-ingredient :region)

(defmethod get-regional-ingredient "NY"
  [_]
  {:cheese    "Reggiano Cheese"
   :dough     "Thin Crust Dough"
   :clams     "Fresh Clams from Long Island Sound"
   :pepperoni "Sliced Pepperoni"                            ;
   :sauce     "Marinara Sauce"
   :vegies    #{"Garlic" "Onion" "Mashroom" "Red Pepper"}})

(defmulti get-kind-ingredients :kind)

(defmethod get-kind-ingredients "cheese"
  [_]
  #{:dough :sauce :cheese})

(defn get-pizza
  [m]
  (select-keys (get-regional-ingredient m) (get-kind-ingredients m)))

(defn- make-log
  [message]
  (fn [x]
    (println message) x))

(def box
  (make-log "box"))

(def cut
  (make-log "cut"))

(def bake
  (make-log "bake"))

(defn- prepare
  [{toppings :toppings :as pizza}]
  (println "prepare")
  (println toppings)
  pizza)

(def transform
  (comp box cut bake prepare))

(def order
  (comp transform get-pizza))

(order {:region "NY"
        :kind   "cheese"})
