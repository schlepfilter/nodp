(ns nodp.jdp.mutex
  (:require [clojure.string :as str]
            [beicon.core :as rx]
            [nodp.helpers :as helpers]))

(def bean-n 1000)

(defn get-thief
  [x]
  (->> (repeat bean-n x)
       rx/from-coll
       (rx/subscribe-on rx/computation)))

(def john (get-thief :john))

(def peter (get-thief :peter))

(def successful-thief
  (->> (rx/merge john peter)
       (rx/take bean-n)
       .publish))

(def describe-theft
  (comp str/capitalize
        (partial (helpers/flip str) " took a bean.")
        name))

(def theft
  (rx/map describe-theft successful-thief))

(helpers/printstream theft)

(defn- make-describe-total
  [thief-k]
  (fn [n]
    (-> [(name thief-k) "took" n "beans"]
        helpers/space-join
        str/capitalize)))

(defn- get-total
  [thief-k]
  (->> successful-thief
       (rx/filter (partial = thief-k))
       .count
       (rx/map (make-describe-total thief-k))))

(def john-total
  (get-total :john))

(helpers/printstream john-total)

(def peter-total
  (get-total :peter))

(helpers/printstream peter-total)

(.connect successful-thief)
