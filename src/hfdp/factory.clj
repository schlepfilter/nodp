(ns hfdp.factory)

(defmulti get-raw identity)

(defmacro defraw
  [pizza]
  `(defmethod get-raw (select-keys ~pizza [:area :kind])
     [_#]
     ~pizza))

(defraw {:area "NY"
         :kind "cheese"
         :name "NY Style Sauce and Cheese Pizza"
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
  (comp transform get-raw))

(order {:area "NY"
        :kind "cheese"})
