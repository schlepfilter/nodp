(ns hfdp.factory)

(defmulti get-pizza identity)

(defn- defpizza
  [pizza]
  (defmethod get-pizza (select-keys pizza [:area :kind])
    [_]
    pizza))

(defpizza {:area     "NY"
           :dough    "Thin Crust Dough"
           :kind     "cheese"
           :name     "NY Style Sauce and Cheese Pizza"
           :sauce    "Marinara Sauce"
           :toppings ["Grated Reggiano Cheese"]})

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

(order {:area "NY"
        :kind "cheese"})
