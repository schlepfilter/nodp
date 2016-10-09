(ns hfdp.facade
  (:require [hfdp.factory :as factory]))

(defn turn-on-amp
  [description]
  (println (str description " on")))

(defn watch-movie
  [movie]
  ((factory/build (fn [& _]) turn-on-amp) "Top-O-Line Amplifier"))

(watch-movie "Raiders of the Lost Ark")
