(ns hfdp.facade
  (:require [hfdp.helpers :as helpers]))

(defn- run-command
  [device command]
  (if (fn? command)
    (command device)
    (apply (first command) device (rest command))))

(defn- run-commands
  [device & commands]
  (-> (partial run-command device)
      (map commands)
      dorun))

(def turn-on
  (comp println
        (partial (helpers/flip str) " on")))

(defn- set-dvd
  [amp dvd]
  (-> (str amp " setting DVD player to " dvd)
      println))

(defn- play
  [dvd film]
  (-> (str dvd " playing " film)
      println))

(defn watch-film
  [{:keys [amp dvd film]}]
  (run-commands amp
                turn-on
                [set-dvd "dvd"])
  (run-commands dvd
                turn-on
                [play film]))

(watch-film {:amp  "Top-O-Line Amplifier"
             :dvd  "Top-O-Line DVD Player"
             :film "Raiders of the Lost Ark"})
