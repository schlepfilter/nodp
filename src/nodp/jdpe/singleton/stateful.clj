(ns nodp.jdpe.singleton.stateful)

(def generator
  (atom 0))

(def set-get-next-serial
  (partial swap! generator inc))

(def print-next-serial
  (comp (partial println "next serial:")
        set-get-next-serial))

(dotimes [_ 3]
  (print-next-serial))
