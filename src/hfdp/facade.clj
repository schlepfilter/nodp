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
  [& device-commands]
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
  (make-get-sentence "off"))

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
