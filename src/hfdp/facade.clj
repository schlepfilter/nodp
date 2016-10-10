(ns hfdp.facade
  (:require [hfdp.helpers :as helpers]))

(def turn-on
  (comp println
        (partial (helpers/flip str) " on")))

(defn set-dvd
  [amp dvd]
  (-> (str amp " setting DVD player to " dvd)
      println))

(defn run-command
  [device [f & more]]
  (apply f device more))

(defn run-commands
  [device & commands]
  (-> (partial run-command device)
      (map commands)
      dorun))

(defn watch-movie
  [{:keys [amp dvd]}]
  (run-commands amp
                [turn-on]
                [set-dvd "dvd"])
  (turn-on dvd))

(watch-movie {:amp   "Top-O-Line Amplifier"
              :dvd   "Top-O-Line DVD Player"
              :movie "Raiders of the Lost Ark"})
