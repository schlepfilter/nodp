(ns nodp.jdp.private-class-data
  (:require [clojure.string :as str]
            [inflections.core :as inflections]
            [com.rpl.specter :as specter]
            [nodp.helpers :as helpers]))

(def stew {:potato 1
           :carrot 2
           :meat   3
           :pepepr 4})

(def get-ingredient
  (comp (partial str/join " ")
        (juxt last
              (comp inflections/plural
                    name
                    first))))

(def get-comma
  (comp (partial str/join ", ")
        drop-last))

(def get-and
  (comp (partial str/join " and ")
        (juxt get-comma last)))

(def mix
  (comp (partial str "Mixing the stew we find: ")
        get-and
        (partial map get-ingredient)))

(def taste
  (partial specter/transform* specter/MAP-VALS dec))

(mix stew)

(def taste-mix
  (juxt (constantly "Tasting the stew")
        (comp mix taste)))

(-> stew
    taste-mix
    helpers/printall)
