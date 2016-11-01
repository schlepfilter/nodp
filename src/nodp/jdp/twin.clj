(ns nodp.jdp.twin)

(defn- get-ball
  [{:keys [draw move]}]
  (future (while true
            (Thread/sleep 250)
            (draw)
            (move))))

(defn print-constantly
  [s]
  (comp println
        (constantly s)))

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
