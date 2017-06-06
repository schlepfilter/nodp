(ns nodp.jdp.mutex
  (:require [clojure.string :as str]
            [aid.core :as aid]
            [beicon.core :as rx]
            [nodp.helpers :as helpers]))

(def bean-n 1000)

(def get-thief
  (comp (partial rx/subscribe-on rx/computation)
        rx/from-coll
        (partial repeat bean-n)))

(def get-thieves
  (partial map get-thief))

(def publish
  (aid/functionize .publish))

(def get-successful-thief-from-thieves
  (comp publish
        (partial rx/take bean-n)
        (partial apply rx/merge)))

(def get-successful-thief
  (comp get-successful-thief-from-thieves
        get-thieves))

(def successful-thief
  (get-successful-thief #{:john :peter}))

(def describe-theft
  (comp str/capitalize
        (partial (aid/flip str) " took a bean.")
        name))

(def get-theft
  (partial rx/map describe-theft))

(helpers/printstream (get-theft successful-thief))

(defn- make-describe-total
  [thief-k]
  (fn [n]
    (-> [(name thief-k) "took" n "beans."]
        helpers/space-join
        str/capitalize)))

(defn- make-get-total
  [successful-thief-stream]
  (aid/build rx/map
             make-describe-total
             (comp (aid/functionize .count)
                   (partial (aid/flip rx/filter) successful-thief-stream)
                   (aid/curry =))))

(def get-total
  (make-get-total successful-thief))

(def print-total
  (comp helpers/printstream get-total))

(print-total :john)

(print-total :peter)

(.connect successful-thief)
