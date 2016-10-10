(ns hfdp.facade
  (:require [hfdp.helpers :as helpers]))

(def turn-on-amp
  (comp println
        (partial (helpers/flip str) " on")))

(defn watch-movie
  [{:keys [amp]}]
  ((juxt turn-on-amp) amp))

(watch-movie {:amp   "Top-O-Line Amplifier"
              :dvd   "Top-O-Line DVD Player"
              :movie "Raiders of the Lost Ark"})
