(ns nodp.jdp.private-class-data
  (:require [clojure.string :as str]
            [inflections.core :as inflections]
            [com.rpl.specter :as specter]
            [nodp.helpers :as helpers]))

(def stew {:potato 1
           :carrot 2
           :meat   3
           :pepepr 4})

(defn- get-ingredient
  [[k v]]
  (->> k
       name
       inflections/plural
       (str v " ")))

(def get-comma
  (comp (partial str/join ", ")
        drop-last))

(def get-and
  (comp
    (partial str/join " and ")
    (juxt get-comma last)))

(def mix
  (comp (partial str "Mixing the stew we find: ")
        get-and
        (partial map get-ingredient)))


(specter/transform specter/MAP-VALS dec stew)

(defn- taste
  [food]
  (specter/transform specter/MAP-VALS dec food))

(mix stew)

(def taste-mix
  (juxt (constantly "Tasting the stew")
        (comp mix taste)))

(-> stew
    taste-mix
    helpers/printall)
