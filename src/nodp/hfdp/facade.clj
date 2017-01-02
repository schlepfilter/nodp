(ns nodp.hfdp.facade
  (:require [nodp.helpers :as helpers]))

(def amp)

(def dvd)

(def film)

(defn- get-action
  [device command]
  (->> (flatten [command])
       (cons device)
       helpers/space-join))

(defn- get-device-actions
  [[device & commands]]
  (-> (partial get-action device)
      (map commands)))

(def get-actions
  (partial mapcat get-device-actions))

(defn- get-arguments
  [{:keys [device-commands description]}]
  (->> device-commands
       get-actions
       (cons description)))

(def print-arguments
  (comp helpers/printall
        get-arguments))

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
  (print-arguments {:description     "Get ready to watch a movie..."
                    :device-commands (get-watch-device-commands)}))

(defn- get-end-device-commands
  []
  [[dvd
    turn-off]])

(defn- end
  []
  (print-arguments {:description     "Shutting movie theater down..."
                    :device-commands (get-end-device-commands)}))

(with-redefs [amp "Top-O-Line Amplifier"
              dvd "Top-O-Line DVD Player"
              film "Raiders of the Lost Ark"]
  (watch)
  (end))

