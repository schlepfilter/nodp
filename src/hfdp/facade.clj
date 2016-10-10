(ns hfdp.facade
  (:require [hfdp.helpers :as helpers]
            [clojure.string :as str]))

(defn- get-arguments
  [device command]
  (if (fn? command)
    (command device)
    (apply (first command) device (rest command))))

(defn- get-arguments-sequence
  [device & commands]
  (-> (partial get-arguments device)
      (map commands)))

(defn- get-sentence
  [& more]
  (str/join " " more))

(def turn-on
  (partial (helpers/flip get-sentence) "on"))

(defn- set-dvd
  [amp dvd]
  (get-sentence amp "setting DVD player to" dvd))

(defn- play
  [dvd film]
  (get-sentence dvd "playing" film))

(defn watch-film
  [{:keys [amp dvd film]}]
  ;TODO extract a function to run multiple run-commands
  (dorun (map println (get-arguments-sequence amp
                                              turn-on
                                              [set-dvd dvd])))
  (dorun (map println (get-arguments-sequence dvd
                                              turn-on
                                              [play film]))))

(watch-film {:amp  "Top-O-Line Amplifier"
             :dvd  "Top-O-Line DVD Player"
             :film "Raiders of the Lost Ark"})
