(ns hfdp.facade
  (:require [clojure.string :as str]
            [cats.core :as m]
            [hfdp.helpers :as helpers]))

(defn- get-action
  [device command]
  (if (fn? command)
    (command device)
    (apply (first command) device (rest command))))

(defn- get-device-actions
  [[device & commands]]
  (-> (partial get-action device)
      (map commands)))

(defn- get-actions
  [& device-commands]
  (mapcat get-device-actions device-commands))

(defn- get-sentence
  [& more]
  (str/join " " more))

(def make-get-subject-verb
  (m/curry 2 (helpers/flip get-sentence)))

(def turn-on
  (make-get-subject-verb "on"))

(defn- set-dvd
  [amp dvd]
  (get-sentence amp "setting DVD player to" dvd))

(defn- play
  [dvd film]
  (get-sentence dvd "playing" film))

(defn watch-film
  [{:keys [amp dvd film]}]
  (->> (get-actions [amp
                     turn-on
                     [set-dvd dvd]]
                    [dvd
                     turn-on
                     [play film]])
       (cons "Get ready to watch a movie...")
       helpers/printall))

(def turn-off
  (make-get-subject-verb "off"))

(defn end-film
  [{:keys [amp dvd film]}]
  (->> (get-actions [dvd
                     turn-off])
       helpers/printall))

(def theater {:amp  "Top-O-Line Amplifier"
              :dvd  "Top-O-Line DVD Player"
              :film "Raiders of the Lost Ark"})

(watch-film theater)

(end-film theater)
