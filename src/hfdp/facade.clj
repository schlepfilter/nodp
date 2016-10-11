(ns hfdp.facade
  (:require [clojure.string :as str]
            [cats.core :as m]
            [hfdp.helpers :as helpers]))

(defn- get-action
  [device command]
  (if (fn? command)
    (command device)
    (((first command) device) (second command))))

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

(def make-get-sv
  (->> (helpers/flip get-sentence)
       (m/curry 2)))

(def turn-on
  (make-get-sv "on"))

(def make-get-svo
  (->> (helpers/flip get-sentence)
       (m/curry 3)))

(def set-dvd
  (make-get-svo "setting DVD player to"))

(defn watch-film
  [{:keys [amp dvd film]}]
  (->> (get-actions [amp
                     turn-on
                     [set-dvd dvd]]
                    [dvd
                     turn-on])
       (cons "Get ready to watch a movie...")
       helpers/printall))

(def turn-off
  (make-get-sv "off"))

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
