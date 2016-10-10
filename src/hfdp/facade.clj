(ns hfdp.facade
  (:require [hfdp.helpers :as helpers]
            [clojure.string :as str]))

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

(defn- print-sentence
  [& more]
  (-> (str/join " " more)
      println))

(defn- set-dvd
  [amp dvd]
  (print-sentence amp "setting DVD player to" dvd))

(defn- play
  [dvd film]
  (print-sentence dvd "playing" film))

(defn watch-film
  [{:keys [amp dvd film]}]
  (run-commands amp
                turn-on
                [set-dvd dvd])
  (run-commands dvd
                turn-on
                [play film]))

(watch-film {:amp  "Top-O-Line Amplifier"
             :dvd  "Top-O-Line DVD Player"
             :film "Raiders of the Lost Ark"})
