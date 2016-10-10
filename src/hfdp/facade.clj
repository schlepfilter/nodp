(ns hfdp.facade
  (:require [hfdp.helpers :as helpers]))

(def turn-on
  (comp println
        (partial (helpers/flip str) " on")))

(defn set-dvd
  [amp dvd]
  (-> (str amp " setting DVD player to " dvd)
      println))

(defn watch-movie
  [{:keys [amp dvd]}]
  (turn-on amp)
  (set-dvd amp dvd)
  (turn-on dvd))

(watch-movie {:amp   "Top-O-Line Amplifier"
              :dvd   "Top-O-Line DVD Player"
              :movie "Raiders of the Lost Ark"})
