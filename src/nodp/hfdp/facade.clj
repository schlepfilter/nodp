(ns nodp.hfdp.facade
  (:require [nodp.helpers :as helpers]))

(def amp)

(def dvd)

(def film)

(def get-action
  (comp helpers/space-join
        flatten
        vector))

(def get-device-actions
  (helpers/build map
                 (comp (helpers/curry get-action) first)
                 rest))

(def get-actions
  (partial mapcat get-device-actions))

(def get-outputs
  (helpers/build cons
                 :description
                 (comp get-actions :device-commands)))

(def print-outputs
  (comp helpers/printall
        get-outputs))

(def play "playing")

(def set-dvd "setting DVD player to")

(def turn-on "on")

(def turn-off "off")

(defn- get-watch-device-commands
  []
  [[amp
    turn-on
    [set-dvd dvd]]
   [dvd
    turn-on
    [play film]]])

(defn- watch
  []
  (print-outputs {:description     "Get ready to watch a movie..."
                  :device-commands (get-watch-device-commands)}))

(defn- get-end-device-commands
  []
  [[dvd
    turn-off]])

(defn- end
  []
  (print-outputs {:description     "Shutting movie theater down..."
                  :device-commands (get-end-device-commands)}))

(with-redefs [amp "Top-O-Line Amplifier"
              dvd "Top-O-Line DVD Player"
              film "Raiders of the Lost Ark"]
  (watch)
  (end))
