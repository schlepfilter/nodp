(ns nodp.jdpe.singleton)

(def generator
  (atom 0))

(def get-next-serial
  (partial swap! generator inc))

(defn print-next-serial
  []
  (println (get-next-serial)))

(dotimes [_ 3]
  (print-next-serial))
