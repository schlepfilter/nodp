(ns nodp.jdp.twin)

(defmacro forever
  [& body]
  `(while true ~@body))

(defn- get-ball
  [{:keys [draw move]}]
  (future (forever
            (Thread/sleep 250)
            (draw)
            (move))))

(def print-constantly
  (comp (partial comp println)
        constantly))

(defn- make-draw
  [do-draw]
  (juxt (print-constantly "draw")
        do-draw))

(def do-draw
  (print-constantly "doDraw"))

(def move
  (print-constantly "move"))

(def suspend
  (juxt (print-constantly "Begin to suspend ball")
        future-cancel))

(let [ball (get-ball {:draw (make-draw do-draw)
                      :move move})]
  (Thread/sleep 750)
  (suspend ball))
