(ns nodp.jdp.private-class-data
  (:require [clojure.string :as str]
            [cats.core :as m]
            [inflections.core :as inflections]
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
  (comp helpers/comma-join
        drop-last))

(def get-and
  (comp (partial str/join " and ")
        (juxt get-comma last)))

(def mix
  (comp (partial str "Mixing the stew we find: ")
        get-and
        (partial map get-ingredient)))

(mix stew)

(def taste
  (partial m/<$> dec))

(def taste-mix
  (juxt (constantly "Tasting the stew")
        (comp mix
              taste)))

(taste-mix stew)
