(ns hfdp.facade
  (:require [clojure.string :as str]
            [hfdp.helpers :as helpers]))

(def actions
  {:play     "playing"
   :set-dvd  "setting DVD player to"
   :turn-off "off"
   :turn-on  "on"})

(defn- get-sentence
  [verb other]
  (->> (conj (rest other) (verb actions) (first other))
       (str/join " ")))

(defn- get-action
  [device command]
  (if (keyword? command)
    (get-sentence command [device])
    (get-sentence (first command) [device (second command)])))

(defn- get-device-actions
  [[device & commands]]
  (-> (partial get-action device)
      (map commands)))

(defn- get-actions
  [device-commands]
  (mapcat get-device-actions device-commands))

(defn- get-arguments
  [{:keys [commands description]}]
  (->> (get-actions commands)
       (cons description)))

(def print-arguments
  (comp helpers/printall
        get-arguments))

(defn watch-film
  [{:keys [amp dvd film]}]
  (-> {:commands    [[amp
                      :turn-on
                      [:set-dvd dvd]]
                     [dvd
                      :turn-on
                      [:play film]]]
       :description "Get ready to watch a movie..."}
      print-arguments))

(defn end-film
  [{:keys [amp dvd film]}]
  (-> {:commands    [[dvd
                      :turn-off]]
       :description "Shutting movie theater down..."}
      print-arguments))

(def theater {:amp  "Top-O-Line Amplifier"
              :dvd  "Top-O-Line DVD Player"
              :film "Raiders of the Lost Ark"})

(watch-film theater)

(end-film theater)
