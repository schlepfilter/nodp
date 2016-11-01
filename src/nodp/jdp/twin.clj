(ns nodp.jdp.twin)

(defn- get-ball
  [{:keys [draw move]}]
  (future (while true
            (Thread/sleep 250)
            (draw)
            (move))))

(defn- make-draw
  [do-draw]
  (juxt (partial println "draw") do-draw))

(def do-draw
  (partial println "doDraw"))

(def move
  (partial println "move"))

(def suspend
  (juxt (comp println
              (constantly "Begin to suspend ball"))
        future-cancel))

(let [ball (get-ball {:draw (make-draw do-draw)
                      :move move})]
  (Thread/sleep 750)
  (suspend ball))
