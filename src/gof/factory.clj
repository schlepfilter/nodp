(ns gof.factory)

(defmulti get-raw identity)

(defmethod get-raw {:area "NY"
                    :kind "cheese"}
  [params]
  (assoc params :toppings ["Grated Reggiano Cheese"]))

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
