(ns hfdp.factory)

(defmulti get-raw identity)

(defmacro defraw
  [{toppings :toppings :as params}]
  `(defmethod get-raw (select-keys ~params [:area :kind])
     [params#]
     (assoc params# :toppings ~toppings)))

(defraw {:area "NY"
         :kind "cheese"
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
  (comp box
        cut
        bake
        prepare))

(def order
  (comp transform
        get-raw))

(order {:area "NY"
        :kind "cheese"})
