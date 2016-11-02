(ns nodp.jdp.twin
  (:require [nodp.helpers :as helpers]))

(defmacro forever
  [& body]
  `(while true ~@body))

(defn- get-ball
  [{:keys [draw move]}]
  (future (forever
            (Thread/sleep 250)
            (draw)
            (move))))

(defn- make-draw
  [do-draw]
  (juxt (helpers/print-constantly "draw")
        do-draw))

(def do-draw
  (helpers/print-constantly "doDraw"))

(def move
  (helpers/print-constantly "move"))

(def suspend
  (juxt (helpers/print-constantly "Begin to suspend ball")
        future-cancel))

(let [ball (get-ball {:draw (make-draw do-draw)
                      :move move})]
  (Thread/sleep 750)
  (suspend ball))
