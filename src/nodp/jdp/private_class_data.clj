(ns nodp.jdp.private-class-data
  (:require [clojure.string :as str]
            [inflections.core :as inflections]))

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

(mix stew)
