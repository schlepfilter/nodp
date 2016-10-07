(ns gof.factory)

(defmulti get-raw identity)

(defmethod get-raw {:area "NY"
                    :kind "cheese"}
  [params]
  (assoc params :toppings ["Grated Reggiano Cheese"]))

(defn- box
  [pizza]
  (println "box")
  pizza)

(defn- cut
  [pizza]
  (println "cut")
  pizza)

(defn- bake
  [pizza]
  (println "bake")
  pizza)

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

(defn- order
  [params]
  (-> (get-raw params)
      transform))

(order {:area "NY"
        :kind "cheese"})
