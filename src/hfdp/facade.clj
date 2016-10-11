(ns hfdp.facade
  (:require [clojure.string :as str]
            [cats.core :as m]
            [hfdp.helpers :as helpers]))

(defn- get-action
  [device command]
  (if (fn? command)
    (command [device])
    ((first command) [device (second command)])))

(defn- get-device-actions
  [[device & commands]]
  (-> (partial get-action device)
      (map commands)))

(defn- get-actions
  [device-commands]
  (mapcat get-device-actions device-commands))

(defmacro defcurried
  [function-name bindings body]
  `(def ~function-name
     (m/curry ~(count bindings) (fn ~bindings ~body))))

(defcurried make-get-sentence
            [verb other]
            (->> (conj (rest other) verb (first other))
                 (str/join " ")))

(def turn-on
  (make-get-sentence "on"))

(def set-dvd
  (make-get-sentence "setting DVD player to"))

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
                      turn-on
                      [set-dvd dvd]]
                     [dvd
                      turn-on]]
       :description "Get ready to watch a movie..."}
      print-arguments))

(def turn-off
  (make-get-sentence "off"))

(def play
  (make-get-sentence "playing"))

(defn end-film
  [{:keys [amp dvd film]}]
  (-> {:commands    [[dvd
                      turn-off
                      [play film]]]
       :description "Shutting movie theater down..."}
      print-arguments))

(def theater {:amp  "Top-O-Line Amplifier"
              :dvd  "Top-O-Line DVD Player"
              :film "Raiders of the Lost Ark"})

(watch-film theater)

(end-film theater)
