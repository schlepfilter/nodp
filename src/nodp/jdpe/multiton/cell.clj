(ns nodp.jdpe.multiton.cell
  (:require [com.rpl.specter :as s]
            [nodp.helpers :as helpers]))

(def generator
  (atom {:engine  0
         :vehicle 0}))

(def get-set-next-serial!
  (comp (partial swap! generator)
        ((helpers/flip (helpers/curry s/transform*)) inc)))

(defn- label
  [k]
  (str "next " (name k) ":"))

(def print-next-serial
  (helpers/build println
                 label
                 get-set-next-serial!))

(dotimes [_ 3]
  (print-next-serial :engine))

(dotimes [_ 3]
  (print-next-serial :vehicle))
