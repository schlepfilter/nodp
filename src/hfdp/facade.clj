(ns hfdp.facade
  (:require [hfdp.helpers :as helpers]
            [clojure.string :as str]))

(defn- get-arguments
  [device command]
  (if (fn? command)
    (command device)
    (apply (first command) device (rest command))))

(defn- get-arguments-sequence
  [[device & commands]]
  (-> (partial get-arguments device)
      (map commands)))

(defn- get-arguments-sequence-sequence
  [& device-commands]
  (mapcat get-arguments-sequence device-commands))

(defn- get-sentence
  [& more]
  (str/join " " more))

(def turn-on
  (-> (helpers/flip get-sentence)
      (partial "on")))

(defn- set-dvd
  [amp dvd]
  (get-sentence amp "setting DVD player to" dvd))

(defn- play
  [dvd film]
  (get-sentence dvd "playing" film))

(defn watch-film
  [{:keys [amp dvd film]}]
  (-> (get-arguments-sequence-sequence [amp
                                        turn-on
                                        [set-dvd dvd]]
                                       [dvd
                                        turn-on
                                        [play film]])
      helpers/printall))

(watch-film {:amp  "Top-O-Line Amplifier"
             :dvd  "Top-O-Line DVD Player"
             :film "Raiders of the Lost Ark"})
