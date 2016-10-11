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
  [f-name bindings body]
  `(def ~f-name
     (->> (fn ~bindings
            ~body)
          (m/curry ~(count bindings)))))

(defcurried make-get-sentence
            [verb other]
            (->> (conj (rest other) verb (first other))
                 (str/join " ")))
(defmacro defall
  [expr]
  `(def _# (doall ~expr)))

(defmacro defaction
  [[action-name verb]]
  `(def ~(symbol (name action-name))
     (make-get-sentence ~verb)))

(defmacro defactions
  [m]
  `(defall (dorun (map (helpers/functionize defaction) ~m))))

(defactions
  {:play     "playing"
   :set-dvd  "setting DVD player to"
   :turn-off "off"
   :turn-on  "on"})

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
                      turn-on
                      [play film]]]
       :description "Get ready to watch a movie..."}
      print-arguments))

(defn end-film
  [{:keys [amp dvd film]}]
  (-> {:commands    [[dvd
                      turn-off
                      ]]
       :description "Shutting movie theater down..."}
      print-arguments))

(def theater {:amp  "Top-O-Line Amplifier"
              :dvd  "Top-O-Line DVD Player"
              :film "Raiders of the Lost Ark"})

(watch-film theater)

(end-film theater)
